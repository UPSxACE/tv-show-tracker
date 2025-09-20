package com.upsxace.tv_show_tracker.genre;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByTmdbId(Long tmdbId);
    List<Genre> findByTmdbIdIn(List<Long> tmdbIds);
}
