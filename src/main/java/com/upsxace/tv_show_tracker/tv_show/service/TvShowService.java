package com.upsxace.tv_show_tracker.tv_show.service;

import com.upsxace.tv_show_tracker.common.jwt.UserContext;
import com.upsxace.tv_show_tracker.data_collector.http.TmdbService;
import com.upsxace.tv_show_tracker.tv_show.graphql.AllTvShowsInput;
import com.upsxace.tv_show_tracker.tv_show.graphql.TvShowDto;
import com.upsxace.tv_show_tracker.tv_show.mapper.TvShowMapper;
import com.upsxace.tv_show_tracker.tv_show.repository.TvShowRepository;
import com.upsxace.tv_show_tracker.user.entity.UserFavoriteTvShow;
import com.upsxace.tv_show_tracker.user.repository.UserFavoriteTvShowRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for managing TV shows, including fetching, mapping to DTOs,
 * and retrieving from external APIs when needed.
 */
@Service
@RequiredArgsConstructor
public class TvShowService {

    private final TvShowRepository tvShowRepository;
    private final TvShowMapper tvShowMapper;
    private final TmdbService tmdbService;
    private final UserFavoriteTvShowRepository userFavoriteTvShowRepository;

    /**
     * Constructs a pageable object for paginated queries, applying optional sorting
     * and limiting page size between 1 and 20.
     *
     * @param input The input containing pagination and sorting information
     * @return Pageable object to use for repository queries
     */
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

    /**
     * Retrieves all TV shows with optional filtering and pagination.
     * Initializes the first show's seasons to prevent lazy loading issues.
     *
     * @param input Input containing filters, sorting, and pagination info
     * @return Page of TV show DTOs
     */
    public Page<TvShowDto> getAll(AllTvShowsInput input) {
        Pageable pageable = createPageable(input);
        Sort sort = pageable.getSort();

        Page<Long> idsPage = (input != null && input.getFilter() != null && input.getFilter().getGenreId() != null)
                ? tvShowRepository.findAllIdsByGenreId(pageable, input.getFilter().getGenreId())
                : tvShowRepository.findAllIds(pageable);

        var tvShows = tvShowRepository.findAllByIdIn(idsPage.getContent(), sort);

        if (!tvShows.isEmpty()) Hibernate.initialize(tvShows.getFirst().getSeasons());

        return new PageImpl<>(
                tvShowMapper.toDtos(tvShows),
                pageable,
                idsPage.getTotalElements()
        );
    }

    /**
     * Retrieves a single TV show by its ID, initializing seasons and fetching
     * actor credits if missing.
     *
     * @param id ID of the TV show
     * @return TV show DTO or null if not found
     */
    public TvShowDto getById(Long id) {
        var tvShow = tvShowRepository.findById(id).orElse(null);
        if (tvShow != null) {
            Hibernate.initialize(tvShow.getSeasons());
            if (tvShow.getActorCredits().isEmpty()) tmdbService.fillTvShowCredits(tvShow);
        }
        return tvShowMapper.toDto(tvShow);
    }

    /**
     * Retrieves multiple TV shows by their IDs.
     *
     * @param ids List of TV show IDs
     * @return List of TV show DTOs
     */
    public List<TvShowDto> getAllById(List<Long> ids) {
        return tvShowMapper.toDtos(tvShowRepository.findAllByIdIn(ids));
    }

    /**
     * Retrieves the list of favorite TV shows for the given user, limited to the specified show IDs.
     * <p>
     * This method queries the persistence layer to find all {@link UserFavoriteTvShow}
     * entities that match both the provided list of TV show IDs and the current user's ID.
     * </p>
     *
     * @param ids      the list of TV show IDs to filter against
     * @param userCtx  the context containing the current user's information (used to extract the user ID)
     * @return a list of {@link UserFavoriteTvShow} entities corresponding to the user's favorites
     *         among the given show IDs; may be empty if none are found
     */
    public List<UserFavoriteTvShow> getUserFavoritesByShowId(List<Long> ids, UserContext userCtx){
        return userFavoriteTvShowRepository.findAllByTvShowIdInAndUserId(ids, userCtx.getId());
    }
}
