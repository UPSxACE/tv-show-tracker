package com.upsxace.tv_show_tracker.actor.graphql;

import com.upsxace.tv_show_tracker.common.graphql.PageInput;
import lombok.Data;

@Data
public class AllActorsInput {
    private final PageInput page;
    private final ActorOrderInput order;
}
