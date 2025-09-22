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

/**
 * Service responsible for interacting with the TMDB API to collect TV show, genre, and actor data.
 * Handles discovery of new shows, genres, and actor credits and synchronizes with the local database.
 */
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

    /**
     * Initializes TMDB discovery state from the database after bean construction.
     */
    @PostConstruct
    public void loadStateFromDatabase() {
        pagesExplored = appPropertyService.readProperty("tmdb:pages-explored").map(Integer::parseInt).orElse(0);
        totalPages = appPropertyService.readProperty("tmdb:total-pages").map(Integer::parseInt).orElse(0);
    }

    /**
     * Saves the current discovery state (pages explored and total pages) to the database.
     */
    public void saveState(int newPagesExplored, int newTotalPages) {
        synchronized (this) {
            pagesExplored = newPagesExplored;
            totalPages = newTotalPages;
            appPropertyService.upsertProperty("tmdb:pages-explored", String.valueOf(newPagesExplored));
            appPropertyService.upsertProperty("tmdb:total-pages", String.valueOf(newTotalPages));
            log.info("Saved discovery state: {}/{} pages explored", newPagesExplored, newTotalPages);
        }
    }

    /**
     * Skips the current page in discovery and updates state.
     */
    public void skipPage() {
        synchronized (this) {
            if(totalPages > pagesExplored) {
                saveState(pagesExplored + 1, totalPages);
            }
        }
    }

    /**
     * Collects all TV show genres from TMDB and stores any new genres in the database.
     */
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

    /**
     * Adds any missing genres to the database based on a list of TMDB genre IDs.
     */
    @Transactional
    private void addMissingGenresToDb(List<Long> genreIds) {
        var idsInDb = genreRepository.findByTmdbIdIn(genreIds).stream().map(Genre::getTmdbId).toList();
        var genresMissing = genreIds.stream().filter(id -> !idsInDb.contains(id)).toList();
        var genres = genresMissing.stream().map(id -> tmdbClient.getTvGenre(id).toModel()).toList();
        genreRepository.saveAll(genres);
    }

    /**
     * Retrieves the top 6 actors in the cast of a TV show from TMDB, sorted by popularity.
     */
    private List<CastPersonDto> getActorsInCast(Long tmdbId){
        return tmdbClient.getTvShowCredits(tmdbId)
                .stream()
                .filter(p -> p.getKnown_for_department().equals("Acting"))
                .sorted(Comparator.comparingDouble(CastPersonDto::getPopularity))
                .limit(6)
                .toList();
    }

    /**
     * Fetches Actor entities for a TV show, creating new Actor models if necessary.
     */
    @Transactional
    private Set<Actor> getActorsInTvShow(Long tmdbId){
        var actorsInCast = getActorsInCast(tmdbId);
        var actorsAlreadyInDb = actorRepository.findByTmdbIdIn(actorsInCast.stream().map(CastPersonDto::getId).toList());

        Set<Actor> actors = new HashSet<>();
        for(var actorInCast : actorsInCast){
            var inDb = actorsAlreadyInDb.stream().filter(a -> a.getTmdbId().equals(actorInCast.getId())).findFirst().orElse(null);
            if(inDb != null){
                actors.add(inDb);
                continue;
            }
            actors.add(actorInCast.toActorModel());
        }

        return actors;
    }

    /**
     * Discovers new TV shows from TMDB and adds them to the database, including genres and actors.
     */
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

            var genreIds = genreService.mapToDbId(tvShowDetails.getGenres().stream().map(GenreDto::getId).toList());
            var model = tvShowDetails.toModel(genreIds);
            tvShowModels.add(model);

            actors.addAll(getActorsInTvShow(tvShowDetails.getId()));
        }

        // save all tv show and actor changes
        tvShowRepository.saveAll(tvShowModels);
        actorRepository.saveAll(actors);

        // update state
        saveState(tvShowsResponse.getPage(), tvShowsResponse.getTotal_pages());
    }

    /**
     * Discovers TV show credits for a given actor and stores new credits in the database.
     */
    @Transactional
    public List<ActorCredit> discoverActorCredits(Actor actor, Long tmdbId){
        log.info("Discovering actor credits...");

        var creditsModels = tmdbClient.getPersonTvShowCredits(tmdbId).stream().map(c -> {
            var tvShow = tvShowRepository.findByTmdbId(c.getId()).orElse(null);
            return c.toModel(tvShow, actor);
        }).toList();

        var creditsInDbIds = actorCreditRepository
                .findByTmdbIdIn(creditsModels.stream().map(ActorCredit::getTmdbId).toList())
                .stream()
                .map(ActorCredit::getTmdbId)
                .toList();

        var newCredits = creditsModels.stream()
                .filter(c -> !creditsInDbIds.contains(c.getTmdbId()))
                .toList();

        actorCreditRepository.saveAllAndFlush(newCredits);
        return newCredits;
    }

    /**
     * Fills actor credits for a given TV show, adding missing actors and linking credits.
     */
    @Transactional
    public void fillTvShowCredits(TvShow tvShow){
        log.info("Discovering tv show credits...");

        var actorsInCast = getActorsInCast(tvShow.getTmdbId());
        var actorsAlreadyInDb = actorRepository.findByTmdbIdIn(actorsInCast.stream().map(CastPersonDto::getId).toList());

        Set<Actor> newActors = new HashSet<>();
        List<ActorCredit> credits = new ArrayList<>();

        for(var actorInCast : actorsInCast){
            var inDb = actorsAlreadyInDb.stream().filter(a -> a.getTmdbId().equals(actorInCast.getId())).findFirst().orElse(null);
            if(inDb != null){
                var newCredits = discoverActorCredits(inDb, inDb.getTmdbId());
                credits.addAll(newCredits);
                continue;
            }
            newActors.add(actorInCast.toActorModel());
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
