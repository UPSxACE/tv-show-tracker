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

/**
 * GraphQL controller for querying actors and their credits.
 * Provides endpoints for retrieving all actors, a specific actor, and their associated credits.
 */
@Controller
@RequiredArgsConstructor
public class ActorController {
    private final ActorService actorService;

    /**
     * Retrieves a paginated list of all actors based on input parameters.
     *
     * @param input pagination and sorting options
     * @return a page of actors
     */
    @QueryMapping
    public Page<Actor> allActors(@Argument AllActorsInput input){
        return actorService.getAll(input);
    }

    /**
     * Retrieves an actor by their ID.
     *
     * @param id the actor ID
     * @return the actor if found, otherwise null
     */
    @QueryMapping
    public Actor getActor(@Argument Long id){
        return actorService.getById(id);
    }

    /**
     * Retrieves all credits for a specific actor.
     *
     * @param actorId the actor ID
     * @return list of actor credits
     */
    @QueryMapping
    public List<ActorCredit> getActorCredits(@Argument Long actorId){
        return actorService.getActorCredits(actorId);
    }
}
