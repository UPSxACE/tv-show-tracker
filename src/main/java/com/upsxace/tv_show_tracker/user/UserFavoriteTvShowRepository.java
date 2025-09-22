package com.upsxace.tv_show_tracker.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserFavoriteTvShowRepository extends JpaRepository<UserFavoriteTvShow, Long> {
    @EntityGraph(attributePaths = "tvShow")
    Page<UserFavoriteTvShow> findAllByUserId(Pageable pageable, UUID userId);
    @EntityGraph(attributePaths = {"tvShow"})
    List<UserFavoriteTvShow> findFirst3ByUserIdOrderByFavoritedAtDesc(UUID userId);
}
