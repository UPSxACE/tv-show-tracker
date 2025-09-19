package com.upsxace.tv_show_tracker.actor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActorRepository extends JpaRepository<Actor, Long> {
    List<Actor> findByTmdbIdIn(List<Long> tmdbIds);
}
