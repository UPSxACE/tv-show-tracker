package com.upsxace.tv_show_tracker.actor.graphql;

import com.upsxace.tv_show_tracker.common.graphql.SortDirection;
import lombok.Data;

@Data
public class ActorOrderInput {
    private final ActorSortableField field;
    private final SortDirection direction;
}
