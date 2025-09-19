package com.upsxace.tv_show_tracker.actor;

import com.upsxace.tv_show_tracker.actor.graphql.AllActorsInput;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ActorController {
    private final ActorService actorService;

    @QueryMapping
    public Page<Actor> allActors(@Argument AllActorsInput input){
        return actorService.getAll(input);
    }
}
