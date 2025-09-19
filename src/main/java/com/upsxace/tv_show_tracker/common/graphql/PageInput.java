package com.upsxace.tv_show_tracker.common.graphql;

import lombok.Data;

@Data
public class PageInput {
    private final Integer page;
    private final Integer size;
}
