package com.upsxace.tv_show_tracker.actor;

import com.upsxace.tv_show_tracker.actor.graphql.AllActorsInput;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActorService {
    private final ActorRepository actorRepository;
    private final ActorCreditRepository actorCreditRepository;

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
}
