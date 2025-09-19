package com.upsxace.tv_show_tracker.tv_show.graphql;

import com.upsxace.tv_show_tracker.common.graphql.SortDirection;
import lombok.Data;

@Data
public class TvShowOrderInput {
    private final TvShowSortableField field;
    private final SortDirection direction;
}
