package com.upsxace.tv_show_tracker.actor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActorCreditRepository extends JpaRepository<ActorCredit, Long> {
    List<ActorCredit> findByTmdbIdIn(List<String> tmdbIds);
}
