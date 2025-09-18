package com.upsxace.tv_show_tracker.data_collector.http;

import com.upsxace.tv_show_tracker.common.app_property.AppPropertyService;
import com.upsxace.tv_show_tracker.data_collector.dto.GenreDto;
import com.upsxace.tv_show_tracker.genre.Genre;
import com.upsxace.tv_show_tracker.genre.GenreRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TmdbService {
    private final AppPropertyService appPropertyService;
    private final TmdbClient tmdbClient;
    private final GenreRepository genreRepository;

    private int pagesExplored = 0;
    private int totalPages = 0;

    @PostConstruct
    public void loadStateFromDatabase(){
        pagesExplored = appPropertyService.readProperty("tmdb:pages-explored").map(Integer::parseInt).orElse(0);
        totalPages = appPropertyService.readProperty("tmdb:total-pages").map(Integer::parseInt).orElse(0);
    }

    public void saveState(int newPagesExplored, int newTotalPages){
        synchronized (this){
            pagesExplored = newPagesExplored;
            totalPages = newTotalPages;
            appPropertyService.upsertProperty("tmdb:pages-explored", String.valueOf(newPagesExplored));
            appPropertyService.upsertProperty("tmdb:total-pages", String.valueOf(newTotalPages));
        }
    }

    public void resetState(){
        synchronized (this){
            pagesExplored = 0;
            totalPages = 0;
            appPropertyService.upsertProperty("tmdb:pages-explored", "0");
            appPropertyService.upsertProperty("tmdb:total-pages", "0");
        }
    }

    @Transactional
    public void collectGenres(){
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
    public void discover(){
        // TODO: discover tv shows
    }
}
