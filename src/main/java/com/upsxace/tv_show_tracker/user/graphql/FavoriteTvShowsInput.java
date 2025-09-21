package com.upsxace.tv_show_tracker.user.graphql;

import com.upsxace.tv_show_tracker.common.graphql.PageInput;
import lombok.Data;

@Data
public class FavoriteTvShowsInput {
    private final PageInput page;
    private final UserFavoriteTvShowOrderInput order;
}
