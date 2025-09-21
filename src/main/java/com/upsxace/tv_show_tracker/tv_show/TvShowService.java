package com.upsxace.tv_show_tracker.tv_show;

import com.upsxace.tv_show_tracker.tv_show.graphql.AllTvShowsInput;
import com.upsxace.tv_show_tracker.tv_show.graphql.TvShowDto;
import com.upsxace.tv_show_tracker.tv_show.mapper.TvShowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TvShowService {
    private final TvShowRepository tvShowRepository;
    private final TvShowMapper tvShowMapper;

    private Pageable createPageable(AllTvShowsInput input) {
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

    public Page<TvShowDto> getAll(AllTvShowsInput input) {
        Pageable pageable = createPageable(input);
        Sort sort = pageable.getSort();
        Page<Long> idsPage;

        if(input != null && input.getFilter() != null && input.getFilter().getGenreId() != null){
            idsPage = tvShowRepository.findAllIdsByGenreId(pageable, input.getFilter().getGenreId());
        } else {
            idsPage = tvShowRepository.findAllIds(pageable);
        }

        var tvShows = tvShowRepository.findAllByIdIn(idsPage.getContent(), sort);

        return new PageImpl<>(
                tvShowMapper.toDtos(tvShows),
                pageable,
                idsPage.getTotalElements()
        );
    }
}
