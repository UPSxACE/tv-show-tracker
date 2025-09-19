package com.upsxace.tv_show_tracker.actor;

import com.upsxace.tv_show_tracker.actor.graphql.AllActorsInput;
import com.upsxace.tv_show_tracker.data_collector.http.TmdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActorService {
    private final ActorRepository actorRepository;
    private final ActorCreditRepository actorCreditRepository;
    private final TmdbService tmdbService;

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

    public Page<Actor> getAll(AllActorsInput input){
        Pageable pageable = createPageable(input);
        return actorRepository.findAll(pageable);
    }

    public List<ActorCredit> getActorCredits(Long actorId){
        var example = Example.of(ActorCredit.builder().actorId(actorId).build());
        var credits = actorCreditRepository.findAll(example);
        if(credits.isEmpty()){
            var actor = actorRepository.findById(actorId).orElseThrow(IllegalStateException::new); // TODO replace by not found error
            tmdbService.discoverCredits(actorId, actor.getTmdbId());
            return actorCreditRepository.findAll(example);
        }
        return credits;
    }
}
