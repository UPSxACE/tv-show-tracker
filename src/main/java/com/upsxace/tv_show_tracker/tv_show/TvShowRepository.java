package com.upsxace.tv_show_tracker.tv_show;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TvShowRepository extends JpaRepository<TvShow, Long> {
    Optional<TvShow> findByTmdbId(Long tmdbId);
    List<TvShow> findByTmdbIdIn(List<Long> tmdbIds);
}
