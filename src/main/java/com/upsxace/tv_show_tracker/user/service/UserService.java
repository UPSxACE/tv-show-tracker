package com.upsxace.tv_show_tracker.user.service;

import com.upsxace.tv_show_tracker.common.exceptions.NotFoundException;
import com.upsxace.tv_show_tracker.common.jwt.UserContext;
import com.upsxace.tv_show_tracker.experience.ExperienceService;
import com.upsxace.tv_show_tracker.mailer.EmailRepository;
import com.upsxace.tv_show_tracker.tv_show.repository.TvShowRepository;
import com.upsxace.tv_show_tracker.user.entity.User;
import com.upsxace.tv_show_tracker.user.entity.UserFavoriteTvShow;
import com.upsxace.tv_show_tracker.user.graphql.FavoriteTvShowsInput;
import com.upsxace.tv_show_tracker.user.graphql.SessionInfo;
import com.upsxace.tv_show_tracker.user.repository.UserFavoriteTvShowRepository;
import com.upsxace.tv_show_tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service class for managing user-related operations such as authentication,
 * managing favorite TV shows, and account deletion.
 *
 * Implements {@link UserDetailsService} for Spring Security authentication support.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TvShowRepository tvShowRepository;
    private final UserFavoriteTvShowRepository userFavoriteTvShowRepository;
    private final ExperienceService experienceService;
    private final EmailRepository emailRepository;

    /**
     * Loads a user by UUID for Spring Security authentication.
     *
     * @param uuid the UUID string of the user
     * @return a Spring Security {@link UserDetails} object
     * @throws UsernameNotFoundException if the user does not exist
     */
    @Override
    public UserDetails loadUserByUsername(String uuid) throws UsernameNotFoundException {
        var user = userRepository.findById(UUID.fromString(uuid))
                .orElseThrow(() -> new BadCredentialsException("Bad credentials."));

        return new org.springframework.security.core.userdetails.User(
                user.getId().toString(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    /**
     * Deletes a user account along with all related data such as emails and favorite TV shows.
     *
     * @param userCtx the context of the user to delete
     */
    @Transactional
    public void deleteAccount(UserContext userCtx) {
        emailRepository.deleteByUserId(userCtx.getId());
        userFavoriteTvShowRepository.deleteByUserId(userCtx.getId());
        userRepository.deleteById(userCtx.getId());
    }

    /**
     * Creates a pageable object for paginated favorite TV shows queries.
     *
     * @param input the input containing page and sort information
     * @return a {@link Pageable} object
     */
    private Pageable createPageable(FavoriteTvShowsInput input) {
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
     * Saves a TV show as a favorite for a user.
     *
     * @param tvShowId the ID of the TV show to favorite
     * @param userCtx  the context of the user performing the action
     */
    @Transactional
    public void saveFavoriteTvShow(Long tvShowId, UserContext userCtx) {
        var tvShow = tvShowRepository.findById(tvShowId).orElseThrow(NotFoundException::new);
        var base = UserFavoriteTvShow.builder()
                .tvShow(tvShow)
                .user(User.builder().id(userCtx.getId()).build())
                .build();
        var userFavorite = userFavoriteTvShowRepository.findOne(Example.of(base)).orElse(base);
        userFavoriteTvShowRepository.save(userFavorite);
        experienceService.reactToFavorite(userCtx.getId());
    }

    /**
     * Removes a TV show from a user's favorites.
     *
     * @param tvShowId the ID of the TV show to remove
     * @param userCtx  the context of the user performing the action
     */
    @Transactional
    public void unfavoriteTvShow(Long tvShowId, UserContext userCtx) {
        var tvShow = tvShowRepository.findById(tvShowId).orElseThrow(NotFoundException::new);
        var base = UserFavoriteTvShow.builder()
                .tvShow(tvShow)
                .user(User.builder().id(userCtx.getId()).build())
                .build();
        var userFavorite = userFavoriteTvShowRepository.findOne(Example.of(base)).orElse(base);
        userFavoriteTvShowRepository.delete(userFavorite);
        experienceService.reactToFavorite(userCtx.getId());
    }

    /**
     * Retrieves a paginated list of a user's favorite TV shows.
     *
     * @param input   the input specifying pagination and sorting options
     * @param userCtx the context of the user
     * @return a {@link Page} of {@link UserFavoriteTvShow} entities
     */
    public Page<UserFavoriteTvShow> getFavoriteShows(FavoriteTvShowsInput input, UserContext userCtx) {
        Pageable pageable = createPageable(input);
        return userFavoriteTvShowRepository.findAllByUserId(pageable, userCtx.getId());
    }

    /**
     * Retrieves the session information for the given user context.
     *
     * <p>This method looks up the user by ID from the {@code UserContext}. If the user
     * does not exist, an {@link IllegalAccessError} is thrown. Otherwise, it constructs
     * and returns a {@link SessionInfo} object containing the user's ID, username, and avatar URL.
     *
     * @param userCtx the context of the user whose session information is to be retrieved
     * @return a {@link SessionInfo} object containing the user's ID, username, and avatar URL
     */
    public SessionInfo getSessionInfo(UserContext userCtx){
        var user = userRepository.findById(userCtx.getId()).orElseThrow(IllegalStateException::new);
        return new SessionInfo(user.getId(), user.getUsername(), user.getAvatarUrl());
    }
}
