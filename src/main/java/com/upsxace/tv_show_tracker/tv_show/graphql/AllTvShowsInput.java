package com.upsxace.tv_show_tracker.tv_show.graphql;

import com.upsxace.tv_show_tracker.common.graphql.PageInput;
import lombok.Data;

@Data
public class AllTvShowsInput {
    private final PageInput page;
    private final TvShowOrderInput order;
}
