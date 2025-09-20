package com.upsxace.tv_show_tracker.tv_show;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TvShowRepository extends JpaRepository<TvShow, Long> {
    @NotNull
    @EntityGraph(attributePaths = {"tvShowGenres", "tvShowGenres.genre"})
    Page<TvShow> findAll(@NotNull Pageable pageable);
    @EntityGraph(attributePaths = {"tvShowGenres", "tvShowGenres.genre"})
    Page<TvShow> findAllByTvShowGenresGenreId(Pageable pageable, Long genreId);
    @NotNull
    @EntityGraph(attributePaths = {"tvShowGenres", "tvShowGenres.genre"})
    List<TvShow> findAllById(@NotNull Iterable<Long> ids);
    Optional<TvShow> findByTmdbId(Long tmdbId);
    List<TvShow> findByTmdbIdIn(List<Long> tmdbIds);
}
