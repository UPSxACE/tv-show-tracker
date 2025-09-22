package com.upsxace.tv_show_tracker.actor.controller;

import com.upsxace.tv_show_tracker.actor.entity.ActorCredit;
import com.upsxace.tv_show_tracker.actor.entity.Actor;
import com.upsxace.tv_show_tracker.actor.graphql.AllActorsInput;
import com.upsxace.tv_show_tracker.actor.service.ActorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ActorController {
    private final ActorService actorService;

    @QueryMapping
    public Page<Actor> allActors(@Argument AllActorsInput input){
        return actorService.getAll(input);
    }

    @QueryMapping
    public Actor getActor(@Argument Long id){
        return actorService.getById(id);
    }

    @QueryMapping
    public List<ActorCredit> getActorCredits(@Argument Long actorId){
        return actorService.getActorCredits(actorId);
    }
}
