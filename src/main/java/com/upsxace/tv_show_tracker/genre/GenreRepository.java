package com.upsxace.tv_show_tracker.genre;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    List<Genre> findByTmdbIdIn(List<Long> tmdbIds);
}
