package com.upsxace.tv_show_tracker.tv_show.repository;

import com.upsxace.tv_show_tracker.tv_show.entity.TvShow;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TvShowRepository extends JpaRepository<TvShow, Long> {
    @NotNull
    @EntityGraph(attributePaths = {"tvShowGenres", "tvShowGenres.genre", "actorCredits", "actorCredits.actor"})
    Optional<TvShow> findById(@NotNull Long id);

    @EntityGraph(attributePaths = {"tvShowGenres", "tvShowGenres.genre", "actorCredits", "actorCredits.actor"})
    List<TvShow> findAllByIdIn(List<Long> ids);
    @EntityGraph(attributePaths = {"tvShowGenres", "tvShowGenres.genre", "actorCredits", "actorCredits.actor"})
    List<TvShow> findAllByIdIn(List<Long> ids, Sort sort);
    @Query("""
        SELECT t.id
        FROM TvShow t
    """)
    Page<Long> findAllIds(Pageable pageable);
    @Query("""
        SELECT t.id
        FROM TvShow t
        JOIN t.tvShowGenres tg
        JOIN tg.genre g
        WHERE g.id = :genreId
    """)
    Page<Long> findAllIdsByGenreId(Pageable pageable, Long genreId);
    Optional<TvShow> findByTmdbId(Long tmdbId);
    List<TvShow> findByTmdbIdIn(List<Long> tmdbIds);

    @Query("""
            SELECT DISTINCT tv FROM TvShow tv
            JOIN FETCH tv.tvShowGenres tg
            WHERE tg.genre.id IN  :genreIds
            AND NOT EXISTS (
            SELECT 1 FROM tv.favoritedBy fb
            WHERE fb.user.id = :userId
         )
       ORDER BY tv.voteAverage DESC
       """)
    List<TvShow> findRecommendations(UUID userId, List<Long> genreIds, Pageable pageable);
}
