package com.upsxace.tv_show_tracker.data_collector.http;

import com.upsxace.tv_show_tracker.actor.entity.Actor;
import com.upsxace.tv_show_tracker.actor.entity.ActorCredit;
import com.upsxace.tv_show_tracker.actor.repository.ActorCreditRepository;
import com.upsxace.tv_show_tracker.actor.repository.ActorRepository;
import com.upsxace.tv_show_tracker.common.app_property.AppPropertyService;
import com.upsxace.tv_show_tracker.data_collector.dto.CastPersonDto;
import com.upsxace.tv_show_tracker.data_collector.dto.GenreDto;
import com.upsxace.tv_show_tracker.data_collector.dto.TvShowDto;
import com.upsxace.tv_show_tracker.genre.Genre;
import com.upsxace.tv_show_tracker.genre.GenreRepository;
import com.upsxace.tv_show_tracker.genre.GenreService;
import com.upsxace.tv_show_tracker.tv_show.entity.TvShow;
import com.upsxace.tv_show_tracker.tv_show.repository.TvShowRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

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
    private final GenreService genreService;

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

    public void skipPage() {
        synchronized (this) {
            if (totalPages > pagesExplored) {
                saveState(pagesExplored + 1, totalPages);
            }
        }
    }

    @Transactional
    public void collectGenres() {
        var allGenres = tmdbClient.getTvGenreList();
        var allIds = allGenres.stream().map(GenreDto::getId).toList();
        var idsInDb = genreRepository.findByTmdbIdIn(allIds).stream()
                .map(Genre::getTmdbId).toList();

        var genresToInsert = allGenres.stream()
                .filter(g -> !idsInDb.contains(g.getId()))
                .map(GenreDto::toModel)
                .toList();

        genreRepository.saveAll(genresToInsert);
    }

    @Transactional
    private void addMissingGenresToDb(List<Long> genreIds) {
        var idsInDb = genreRepository.findByTmdbIdIn(genreIds).stream()
                .map(Genre::getTmdbId).collect(Collectors.toSet());
        var genresMissing = genreIds.stream().filter(id -> !idsInDb.contains(id)).toList();
        var genres = genresMissing.stream().map(id -> tmdbClient.getTvGenre(id).toModel()).toList();
        genreRepository.saveAll(genres);
    }

    private List<CastPersonDto> getActorsInCast(Long tmdbId){
        return tmdbClient.getTvShowCredits(tmdbId)
                .stream()
                .filter(p -> "Acting".equals(p.getKnown_for_department()))
                .sorted(Comparator.comparingDouble(CastPersonDto::getPopularity).reversed()) // descending
                .limit(6)
                .toList();
    }

    @Transactional
    private Set<Actor> getActorsInTvShow(Long tmdbId){
        var actorsInCast = getActorsInCast(tmdbId);
        var actorsAlreadyInDb = actorRepository.findByTmdbIdIn(
                actorsInCast.stream().map(CastPersonDto::getId).toList()
        );

        Set<Actor> actors = new HashSet<>();
        for (var actorInCast : actorsInCast) {
            var inDb = actorsAlreadyInDb.stream()
                    .filter(a -> a.getTmdbId().equals(actorInCast.getId()))
                    .findFirst()
                    .orElse(null);
            actors.add(inDb != null ? inDb : actorInCast.toActorModel());
        }

        return actors;
    }

    @Transactional
    public void discover() {
        if (totalPages > 0 && pagesExplored >= totalPages) return;

        log.info("Discovering...");
        var tvShowsResponse = tmdbClient.getTvShows(pagesExplored + 1);

        var tmdbIds = tvShowsResponse.getResults().stream().map(TvShowDto::getId).toList();
        var tvShowModels = tvShowRepository.findByTmdbIdIn(tmdbIds);
        var inDbIds = tvShowModels.stream().map(TvShow::getTmdbId).toList();

        var tvShowsToAddIds = tmdbIds.stream().filter(id -> !inDbIds.contains(id)).toList();
        Set<Actor> allActors = new HashSet<>();

        for (var tvShowDetails : tvShowsToAddIds.stream().map(tmdbClient::getTvShowDetails).toList()) {

            // Add any missing genres to DB
            addMissingGenresToDb(tvShowDetails.getGenres().stream().map(GenreDto::getId).toList());

            // Map genres to DB IDs
            var genreIds = genreService.mapToDbId(tvShowDetails.getGenres().stream().map(GenreDto::getId).toList());
            var model = tvShowDetails.toModel(genreIds);
            tvShowModels.add(model);

            // Collect all actors from cast
            allActors.addAll(getActorsInTvShow(tvShowDetails.getId()));
        }

        // Batch save TV shows and actors
        tvShowRepository.saveAll(tvShowModels);
        actorRepository.saveAll(allActors);

        // Update discovery state
        saveState(tvShowsResponse.getPage(), tvShowsResponse.getTotal_pages());
    }


    @Transactional
    public List<ActorCredit> discoverActorCredits(Actor actor, Long tmdbId){
        log.info("Discovering actor credits...");

        var creditsModels = tmdbClient.getPersonTvShowCredits(tmdbId).stream()
                .map(c -> c.toModel(tvShowRepository.findByTmdbId(c.getId()).orElse(null), actor))
                .toList();

        var creditsInDbIds = actorCreditRepository.findByTmdbIdIn(
                creditsModels.stream().map(ActorCredit::getTmdbId).toList()
        ).stream().map(ActorCredit::getTmdbId).toList();

        var newCredits = creditsModels.stream()
                .filter(c -> !creditsInDbIds.contains(c.getTmdbId()))
                .toList();

        actorCreditRepository.saveAllAndFlush(newCredits);
        return newCredits;
    }

    @Transactional
    public void fillTvShowCredits(TvShow tvShow){
        log.info("Discovering tv show credits...");

        var actorsInCast = getActorsInCast(tvShow.getTmdbId());
        var actorsAlreadyInDb = actorRepository.findByTmdbIdIn(
                actorsInCast.stream().map(CastPersonDto::getId).toList()
        );

        Map<Long, Actor> actorsMap = new HashMap<>();
        for (var actor : actorsAlreadyInDb) {
            actorsMap.put(actor.getTmdbId(), actor);
        }

        Set<Actor> newActors = new HashSet<>();
        List<ActorCredit> credits = new ArrayList<>();

        for(var actorInCast : actorsInCast){
            Actor inDb = actorsMap.get(actorInCast.getId());
            if (inDb != null){
                credits.addAll(discoverActorCredits(inDb, inDb.getTmdbId()));
            } else {
                newActors.add(actorInCast.toActorModel());
            }
        }

        actorRepository.saveAll(newActors);

        var creditsFromCurrent = credits.stream()
                .filter(c -> c.getTvShowTmdbId().equals(tvShow.getTmdbId()))
                .toList();

        creditsFromCurrent.forEach(c -> {
            if(c.getTvShow() == null){
                c.setTvShow(tvShow);
            }
            c.setTvShowId(c.getTvShow().getId());
            c.setActorId(c.getActor().getId());
        });

        actorCreditRepository.saveAllAndFlush(credits);
        tvShow.setActorCredits(creditsFromCurrent);

        if(!credits.isEmpty()) Hibernate.initialize(credits.getFirst().getActor());
    }

}
