package com.upsxace.tv_show_tracker.common.app_property;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface AppPropertyRepository extends JpaRepository<AppProperty, Long>, QueryByExampleExecutor<AppProperty> {
}
