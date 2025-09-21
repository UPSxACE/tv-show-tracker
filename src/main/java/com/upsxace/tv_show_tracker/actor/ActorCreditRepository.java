package com.upsxace.tv_show_tracker.actor;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActorCreditRepository extends JpaRepository<ActorCredit, Long> {
    @NotNull
    @Override
    @EntityGraph(attributePaths = {"actor"})
    <S extends ActorCredit> List<S> findAll(@NotNull Example<S> example);

    List<ActorCredit> findByTmdbIdIn(List<String> tmdbIds);

    @EntityGraph(attributePaths = {"actor"})
    List<ActorCredit> findByTvShowIdIn(List<Long> tvShowIds);
}
