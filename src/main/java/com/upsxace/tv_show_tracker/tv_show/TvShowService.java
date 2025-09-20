package com.upsxace.tv_show_tracker.tv_show;

import com.upsxace.tv_show_tracker.tv_show.graphql.AllTvShowsInput;
import com.upsxace.tv_show_tracker.tv_show.graphql.TvShowDto;
import com.upsxace.tv_show_tracker.tv_show.mapper.TvShowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

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

    private Page<TvShowDto> getAllByGenreId(Pageable pageable, Long genreId){
        // NOTE: Query was split into 2 intentionally, to avoid pagination-related
        // warnings/performance issues due to many-to-many relationships.

        // Get paged IDs
        Page<Long> idsPage = tvShowRepository.findAllIdsByGenreId(pageable, genreId);
        List<Long> ids = idsPage.getContent();

        // Fetch entities with collections
        List<TvShow> tvShows = ids.isEmpty() ? List.of() : tvShowRepository.findAllByIdInAndTvShowGenresGenreId(ids, genreId);

        // Map to DTO page
        return new PageImpl<>(
                tvShowMapper.toDtos(tvShows),
                pageable,
                idsPage.getTotalElements()
        );
    }

    private  Page<TvShowDto> getAll(Pageable pageable){
        // NOTE: Query was split into 2 intentionally, to avoid pagination-related
        // warnings/performance issues due to many-to-many relationships.

        // Get paged IDs
        Page<Long> idsPage = tvShowRepository.findAllIds(pageable);
        List<Long> ids = idsPage.getContent();

        // Fetch entities with collections
        List<TvShow> tvShows = ids.isEmpty() ? List.of() : tvShowRepository.findAllById(ids);

        // Map to DTO page
        return new PageImpl<>(
                tvShowMapper.toDtos(tvShows),
                pageable,
                idsPage.getTotalElements()
        );
    }

    public Page<TvShowDto> getAll(AllTvShowsInput input) {
        Pageable pageable = createPageable(input);

        if(input.getFilter() != null && input.getFilter().getGenreId() != null)
            return getAllByGenreId(pageable, input.getFilter().getGenreId());

        return getAll(pageable);
    }
}
