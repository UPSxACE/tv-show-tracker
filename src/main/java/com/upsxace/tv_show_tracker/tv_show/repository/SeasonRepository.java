package com.upsxace.tv_show_tracker.tv_show.repository;

import com.upsxace.tv_show_tracker.tv_show.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeasonRepository extends JpaRepository<Season, Long> {
}
