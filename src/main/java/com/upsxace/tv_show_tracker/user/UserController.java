package com.upsxace.tv_show_tracker.user;

import com.upsxace.tv_show_tracker.common.jwt.AuthService;
import com.upsxace.tv_show_tracker.common.jwt.JwtConfig;
import com.upsxace.tv_show_tracker.common.jwt.UserContext;
import com.upsxace.tv_show_tracker.common.jwt.utils.TokenCookieUtils;
import com.upsxace.tv_show_tracker.tv_show.TvShowService;
import com.upsxace.tv_show_tracker.tv_show.graphql.TvShowDto;
import com.upsxace.tv_show_tracker.user.graphql.FavoriteTvShowsInput;
import com.upsxace.tv_show_tracker.user.graphql.JwtResponse;
import com.upsxace.tv_show_tracker.user.graphql.LoginUserInput;
import com.upsxace.tv_show_tracker.user.graphql.RegisterUserInput;
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

@Controller
@RequiredArgsConstructor
public class UserController {
    private final AuthService authService;
    private final JwtConfig jwtConfig;
    private final TvShowService tvShowService;
    private final UserService userService;

    @MutationMapping
    public boolean registerUser(@Argument @Valid RegisterUserInput input){
        return authService.register(input);
    }

    @MutationMapping
    public JwtResponse loginUser(@Argument @Valid LoginUserInput input, GraphQLContext context){
        var result = authService.login(input);
        context.put("refreshToken", TokenCookieUtils.getRefreshTokenCookie(jwtConfig, result.getRefreshToken()));
        context.put("accessToken", TokenCookieUtils.getAccessTokenCookie(jwtConfig, result.getAccessToken()));
        return new JwtResponse(result.getAccessToken());
    }

    @MutationMapping
    public JwtResponse refreshToken(@ContextValue Optional<String> currentRefreshToken, GraphQLContext context){
        if(currentRefreshToken.isEmpty())
            return null;

        var result = authService.refreshToken(currentRefreshToken.get()).orElse(null);
        if(result == null)
            return null;

        context.put("accessToken", TokenCookieUtils.getRefreshTokenCookie(jwtConfig, result.getAccessToken()));
        return new JwtResponse(result.getAccessToken());
    }

    @MutationMapping
    public boolean logout(GraphQLContext context){
        context.put("accessToken", TokenCookieUtils.getAccessTokenDeleteCookie(jwtConfig));
        context.put("refreshToken", TokenCookieUtils.getRefreshTokenDeleteOookie(jwtConfig));
        return true;
    }

    @MutationMapping
    @Secured("ROLE_USER")
    public boolean saveFavoriteTvShow(@Argument Long tvShowId, @ContextValue(required = false) UserContext userCtx){
        userService.saveFavoriteTvShow(tvShowId, userCtx);
        return true;
    }

    @MutationMapping
    @Secured("ROLE_USER")
    public boolean unfavoriteTvShow(@Argument Long tvShowId, @ContextValue(required = false) UserContext userCtx){
        userService.unfavoriteTvShow(tvShowId, userCtx);
        return true;
    }

    @QueryMapping
    @Secured("ROLE_USER")
    public Page<UserFavoriteTvShow> favoriteTvShows(@Argument FavoriteTvShowsInput input, @ContextValue(required = false) UserContext userCtx){
        return userService.getFavoriteShows(input, userCtx);
    }

    @BatchMapping(typeName = "UserFavoriteTvShow")
    public List<TvShowDto> tvShow(List<UserFavoriteTvShow> userFavoriteTvShows){
        var allTvShowIds = userFavoriteTvShows.stream().map(u -> u.getTvShow().getId()).toList();
        var allTvShows = tvShowService.getAllById(allTvShowIds);

        var tvShowsById = allTvShows.stream().collect(
                Collectors.toMap(
                        TvShowDto::getId,
                        t -> t
                )
        );

        return userFavoriteTvShows.stream()
                .map(u -> tvShowsById.get(
                        u.getTvShow().getId()
                ))
                .toList();
    }
}
