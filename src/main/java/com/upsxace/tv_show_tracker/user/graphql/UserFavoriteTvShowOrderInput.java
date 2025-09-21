package com.upsxace.tv_show_tracker.user.graphql;

import com.upsxace.tv_show_tracker.common.graphql.SortDirection;
import lombok.Data;

@Data
public class UserFavoriteTvShowOrderInput {
    private final UserFavoriteTvShowSortableField field;
    private final SortDirection direction;
}
