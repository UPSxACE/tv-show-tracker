package com.upsxace.tv_show_tracker.data_collector.http;

import com.upsxace.tv_show_tracker.actor.Actor;
import com.upsxace.tv_show_tracker.actor.ActorCredit;
import com.upsxace.tv_show_tracker.actor.ActorCreditRepository;
import com.upsxace.tv_show_tracker.actor.ActorRepository;
import com.upsxace.tv_show_tracker.common.app_property.AppPropertyService;
import com.upsxace.tv_show_tracker.data_collector.dto.CastPersonDto;
import com.upsxace.tv_show_tracker.data_collector.dto.GenreDto;
import com.upsxace.tv_show_tracker.data_collector.dto.TvShowDto;
import com.upsxace.tv_show_tracker.genre.Genre;
import com.upsxace.tv_show_tracker.genre.GenreRepository;
import com.upsxace.tv_show_tracker.tv_show.TvShow;
import com.upsxace.tv_show_tracker.tv_show.TvShowRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbService {
    private final AppPropertyService appPropertyService;
    private final TmdbClient tmdbClient;
    private final GenreRepository genreRepository;
    private final TvShowRepository tvShowRepository;
    private final ActorRepository actorRepository;
    private final ActorCreditRepository actorCreditRepository;

    private int pagesExplored = 0;
    private int totalPages = 0;

    @PostConstruct
    public void loadStateFromDatabase() {
        pagesExplored = appPropertyService.readProperty("tmdb:pages-explored").map(Integer::parseInt).orElse(0);
        totalPages = appPropertyService.readProperty("tmdb:total-pages").map(Integer::parseInt).orElse(0);
    }

    public void saveState(int newPagesExplored, int newTotalPages) {
        synchronized (this) {
            pagesExplored = newPagesExplored;
            totalPages = newTotalPages;
            appPropertyService.upsertProperty("tmdb:pages-explored", String.valueOf(newPagesExplored));
            appPropertyService.upsertProperty("tmdb:total-pages", String.valueOf(newTotalPages));
            log.info("Saved discovery state: {}/{} pages explored", newPagesExplored, newTotalPages);
        }
    }

    public void resetState() {
        synchronized (this) {
            pagesExplored = 0;
            totalPages = 0;
            appPropertyService.upsertProperty("tmdb:pages-explored", "0");
            appPropertyService.upsertProperty("tmdb:total-pages", "0");
            log.info("Discovery state has been reset.");
        }
    }

    @Transactional
    public void collectGenres() {
        var allGenres = tmdbClient.getTvGenreList();
        var allIds = allGenres.stream().map(GenreDto::getId).toList();
        var idsInDb = genreRepository.findByTmdbIdIn(allIds).stream().map(Genre::getTmdbId).toList();

        var genresToInsert = allGenres
                .stream()
                .filter(g -> !idsInDb.contains(g.getId()))
                .map(GenreDto::toModel)
                .toList();

        genreRepository.saveAll(genresToInsert);
    }

    @Transactional
    private void addMissingGenresToDb(List<Long> genreIds) {
        var idsInDb = genreRepository.findByTmdbIdIn(genreIds).stream().map(Genre::getTmdbId).toList();
        var genresMissing = genreIds.stream().filter(id -> !idsInDb.contains(id)).toList();
        var genres = genresMissing.stream().map(id -> tmdbClient.getTvGenre(id).toModel()).toList();
        genreRepository.saveAll(genres);
    }

    @Transactional
    public void discover() {
        if (totalPages > 0 && pagesExplored >= totalPages)
            return;

        // fetch next page of tv shows
        log.info("Discovering...");
        var tvShowsResponse = tmdbClient.getTvShows(pagesExplored + 1);

        var tmdbIds = tvShowsResponse.getResults().stream().map(TvShowDto::getId).toList();
        var tvShowModels = tvShowRepository.findByTmdbIdIn(tmdbIds);
        var inDbIds = tvShowModels.stream().map(TvShow::getTmdbId).toList();
        var tvShowsToAddIds = tmdbIds.stream().filter(id -> !inDbIds.contains(id)).toList();

        var tvShowDetailsList = tvShowsToAddIds.stream().map(tmdbClient::getTvShowDetails).toList();
        Set<Actor> actors = new HashSet<>();
        for (var tvShowDetails : tvShowDetailsList) {
            // if the tv show has any genre that is not in the database yet, add it
            addMissingGenresToDb(tvShowDetails.getGenres().stream().map(GenreDto::getId).toList());

            var model = tvShowDetails.toModel();
            tvShowModels.add(model);

            // fetch cast, and filter to get only actors
            var actorsInCast = tmdbClient.getTvShowCredits(tvShowDetails.getId())
                    .stream()
                    .filter(p -> p.getKnown_for_department().equals("Acting"))
                    .sorted((p1, p2) -> (int) (p1.getPopularity() - p2.getPopularity()))
                    .limit(6) // for now, only gather the 6 most popular actors
                    .toList();

            var actorsAlreadyInDb = actorRepository.findByTmdbIdIn(actorsInCast.stream().map(CastPersonDto::getId).toList());

            for(var actorInCast : actorsInCast){
                var inDb = actorsAlreadyInDb.stream().filter(a -> a.getTmdbId().equals(actorInCast.getId())).findFirst().orElse(null);
                if(inDb != null){
                    actors.add(inDb);
                    continue;
                }
                // if actor not in database, then create new model
                var newActorModel = actorInCast.toActorModel();
                actors.add(newActorModel);
            }
        }

        // save all tv show and actor changes
        tvShowRepository.saveAll(tvShowModels);
        actorRepository.saveAll(actors);

        // update state
        saveState(tvShowsResponse.getPage(), tvShowsResponse.getTotal_pages());
    }

    @Transactional
    public void discoverCredits(Long actorId, Long tmdbId){
        log.info("Discovering credits...");

        var creditsModels = tmdbClient.getPersonTvShowCredits(tmdbId).stream().map(c -> {
            var tvShowId = tvShowRepository.findByTmdbId(c.getId()).map(TvShow::getId).orElse(null);
            return c.toModel(tvShowId, actorId);
        }).toList();

        var creditsInDbIds = actorCreditRepository
                .findByTmdbIdIn(creditsModels.stream().map(ActorCredit::getTmdbId).toList())
                .stream()
                .map(ActorCredit::getTmdbId)
                .toList();

        var newCredits = creditsModels.stream()
                .filter(c -> !creditsInDbIds.contains(c.getTmdbId()))
                .toList();

        actorCreditRepository.saveAll(newCredits);
    }
}
