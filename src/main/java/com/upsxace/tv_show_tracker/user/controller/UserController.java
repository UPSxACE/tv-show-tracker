package com.upsxace.tv_show_tracker.user.controller;

import com.upsxace.tv_show_tracker.common.jwt.AuthService;
import com.upsxace.tv_show_tracker.common.jwt.JwtConfig;
import com.upsxace.tv_show_tracker.common.jwt.UserContext;
import com.upsxace.tv_show_tracker.common.jwt.utils.TokenCookieUtils;
import com.upsxace.tv_show_tracker.tv_show.service.TvShowService;
import com.upsxace.tv_show_tracker.tv_show.graphql.TvShowDto;
import com.upsxace.tv_show_tracker.user.entity.UserFavoriteTvShow;
import com.upsxace.tv_show_tracker.user.service.UserService;
import com.upsxace.tv_show_tracker.user.graphql.*;
import graphql.GraphQLContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for handling GraphQL requests related to user operations.
 * Includes authentication, managing favorite TV shows, and account deletion.
 */
@Controller
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final JwtConfig jwtConfig;
    private final TvShowService tvShowService;
    private final UserService userService;

    /**
     * Registers a new user.
     *
     * @param input the registration input data
     * @return true if registration is successful
     */
    @MutationMapping
    public boolean registerUser(@Argument @Valid RegisterUserInput input) {
        return authService.register(input);
    }

    /**
     * Logs in a user and returns a JWT access token.
     * Adds refresh and access tokens to the GraphQL context as cookies.
     *
     * @param input   the login input containing username/password
     * @param context the GraphQL context to set token cookies
     * @return a {@link JwtResponse} containing the access token
     */
    @MutationMapping
    public JwtResponse loginUser(@Argument @Valid LoginUserInput input, GraphQLContext context) {
        var result = authService.login(input);
        context.put("refreshToken", TokenCookieUtils.getRefreshTokenCookie(jwtConfig, result.getRefreshToken()));
        context.put("accessToken", TokenCookieUtils.getAccessTokenCookie(jwtConfig, result.getAccessToken()));
        return new JwtResponse(result.getAccessToken());
    }

    /**
     * Refreshes the JWT access token using the current refresh token.
     *
     * @param currentRefreshToken the optional current refresh token
     * @param context             the GraphQL context to set new access token cookie
     * @return a {@link JwtResponse} with a new access token, or null if refresh fails
     */
    @MutationMapping
    public JwtResponse refreshToken(@ContextValue Optional<String> currentRefreshToken, GraphQLContext context) {
        if (currentRefreshToken.isEmpty()) return null;

        var result = authService.refreshToken(currentRefreshToken.get()).orElse(null);
        if (result == null) return null;

        context.put("accessToken", TokenCookieUtils.getRefreshTokenCookie(jwtConfig, result.getAccessToken()));
        return new JwtResponse(result.getAccessToken());
    }

    /**
     * Logs out the current user by deleting access and refresh token cookies.
     *
     * @param context the GraphQL context to delete token cookies
     * @return true if logout is successful
     */
    @MutationMapping
    public boolean logout(GraphQLContext context) {
        context.put("accessToken", TokenCookieUtils.getAccessTokenDeleteCookie(jwtConfig));
        context.put("refreshToken", TokenCookieUtils.getRefreshTokenDeleteOookie(jwtConfig));
        return true;
    }

    /**
     * Saves a TV show as a favorite for the current user.
     *
     * @param tvShowId the ID of the TV show to favorite
     * @param userCtx  the current user context
     * @return true if the operation is successful
     */
    @MutationMapping
    @Secured("ROLE_USER")
    public boolean saveFavoriteTvShow(@Argument Long tvShowId, @ContextValue(required = false) UserContext userCtx) {
        userService.saveFavoriteTvShow(tvShowId, userCtx);
        return true;
    }

    /**
     * Removes a TV show from the current user's favorites.
     *
     * @param tvShowId the ID of the TV show to remove
     * @param userCtx  the current user context
     * @return true if the operation is successful
     */
    @MutationMapping
    @Secured("ROLE_USER")
    public boolean unfavoriteTvShow(@Argument Long tvShowId, @ContextValue(required = false) UserContext userCtx) {
        userService.unfavoriteTvShow(tvShowId, userCtx);
        return true;
    }

    /**
     * Retrieves a paginated list of the current user's favorite TV shows.
     *
     * @param input   the input specifying pagination and sorting
     * @param userCtx the current user context
     * @return a {@link Page} of {@link UserFavoriteTvShow}
     */
    @QueryMapping
    @Secured("ROLE_USER")
    public Page<UserFavoriteTvShow> favoriteTvShows(@Argument FavoriteTvShowsInput input, @ContextValue(required = false) UserContext userCtx) {
        return userService.getFavoriteShows(input, userCtx);
    }

    /**
     * Resolves the actual TV show details for a batch of UserFavoriteTvShow objects.
     *
     * @param userFavoriteTvShows the list of UserFavoriteTvShow entities
     * @return a list of {@link TvShowDto} objects corresponding to the favorites
     */
    @BatchMapping(typeName = "UserFavoriteTvShow")
    public List<TvShowDto> tvShow(List<UserFavoriteTvShow> userFavoriteTvShows) {
        var allTvShowIds = userFavoriteTvShows.stream().map(u -> u.getTvShow().getId()).toList();
        var allTvShows = tvShowService.getAllById(allTvShowIds);

        var tvShowsById = allTvShows.stream().collect(Collectors.toMap(TvShowDto::getId, t -> t));

        return userFavoriteTvShows.stream()
                .map(u -> tvShowsById.get(u.getTvShow().getId()))
                .toList();
    }

    /**
     * Deletes the current user's account if confirmation is provided.
     * Also deletes the access and refresh token cookies to log the user out.
     *
     * @param input   the confirmation input
     * @param userCtx the current user context
     * @param context the GraphQL context to delete token cookies
     * @return true if the account is deleted, false if confirmation was not provided
     */
    @MutationMapping
    @Secured("ROLE_USER")
    public boolean deleteAccount(@Argument ConfirmInput input, @ContextValue(required = false) UserContext userCtx, GraphQLContext context) {
        if (!input.isConfirm()) {
            return false;
        }

        userService.deleteAccount(userCtx);
        context.put("accessToken", TokenCookieUtils.getAccessTokenDeleteCookie(jwtConfig));
        context.put("refreshToken", TokenCookieUtils.getRefreshTokenDeleteOookie(jwtConfig));
        return true;
    }
}
