package com.upsxace.tv_show_tracker.actor.service;

import com.upsxace.tv_show_tracker.actor.entity.ActorCredit;
import com.upsxace.tv_show_tracker.actor.entity.Actor;
import com.upsxace.tv_show_tracker.actor.graphql.AllActorsInput;
import com.upsxace.tv_show_tracker.actor.repository.ActorCreditRepository;
import com.upsxace.tv_show_tracker.actor.repository.ActorRepository;
import com.upsxace.tv_show_tracker.data_collector.http.TmdbService;
import com.upsxace.tv_show_tracker.tv_show.repository.TvShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing actors and their credits.
 * Provides methods to fetch actors, paginate results, and retrieve actor credits.
 */
@Service
@RequiredArgsConstructor
public class ActorService {
    private final ActorRepository actorRepository;
    private final ActorCreditRepository actorCreditRepository;
    private final TmdbService tmdbService;
    private final TvShowRepository tvShowRepository;

    /**
     * Creates a pageable object for paginated queries based on input parameters.
     *
     * @param input the input containing pagination and sorting information
     * @return a Pageable object
     */
    private Pageable createPageable(AllActorsInput input) {
        int page = 0;
        if (input != null && input.getPage() != null && input.getPage().getPage() != null) {
            page = input.getPage().getPage();
        }

        Sort sort = Sort.unsorted();
        if (input != null && input.getOrder() != null) {
            var orderInput = input.getOrder();
            Sort.Direction direction = Sort.Direction.ASC;
            if (orderInput.getDirection() != null) {
                direction = Sort.Direction.valueOf(orderInput.getDirection().name());
            }
            String sortField = orderInput.getField().name();
            sort = Sort.by(direction, sortField);
        }

        int pageSize = (input != null && input.getPage() != null && input.getPage().getSize() != null)
                ? Math.max(1, Math.min(20, input.getPage().getSize()))
                : 20;

        return PageRequest.of(page, pageSize, sort);
    }

    /**
     * Retrieves a paginated list of all actors.
     *
     * @param input pagination and sorting parameters
     * @return a page of actors
     */
    public Page<Actor> getAll(AllActorsInput input){
        Pageable pageable = createPageable(input);
        return actorRepository.findAll(pageable);
    }

    /**
     * Fetches an actor by its ID.
     *
     * @param id the actor ID
     * @return the actor if found, otherwise null
     */
    public Actor getById(Long id){
        return actorRepository.findById(id).orElse(null);
    }

    /**
     * Retrieves all credits for a given actor.
     * If no credits are present in the database, fetches them from TMDB and saves.
     *
     * @param actorId the actor ID
     * @return a list of actor credits
     */
    public List<ActorCredit> getActorCredits(Long actorId){
        var example = Example.of(ActorCredit.builder().actor(Actor.builder().id(actorId).build()).build());
        var credits = actorCreditRepository.findAll(example);

        if(credits.isEmpty()){
            var actor = actorRepository.findById(actorId).orElseThrow(IllegalStateException::new); // TODO replace by not found error
            tmdbService.discoverActorCredits(actor, actor.getTmdbId());
            return actorCreditRepository.findAll(example);
        }

        return credits;
    }

    /**
     * Retrieves actor credits for a list of TV show IDs.
     *
     * @param tvShowIds list of TV show IDs
     * @return a list of actor credits
     */
    public List<ActorCredit> getActorCreditsByTvShowIds(List<Long> tvShowIds){
        return actorCreditRepository.findByTvShowIdIn(tvShowIds);
    }
}
