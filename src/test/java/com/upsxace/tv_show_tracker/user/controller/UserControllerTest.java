package com.upsxace.tv_show_tracker.user.controller;

import com.upsxace.tv_show_tracker.common.jwt.AuthService;
import com.upsxace.tv_show_tracker.common.jwt.JwtConfig;
import com.upsxace.tv_show_tracker.common.jwt.UserContext;
import com.upsxace.tv_show_tracker.common.jwt.utils.LoginResult;
import com.upsxace.tv_show_tracker.common.jwt.utils.RefreshResult;
import com.upsxace.tv_show_tracker.tv_show.entity.TvShow;
import com.upsxace.tv_show_tracker.tv_show.service.TvShowService;
import com.upsxace.tv_show_tracker.tv_show.graphql.TvShowDto;
import com.upsxace.tv_show_tracker.user.entity.UserFavoriteTvShow;
import com.upsxace.tv_show_tracker.user.graphql.*;
import com.upsxace.tv_show_tracker.user.service.UserService;
import graphql.GraphQLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private AuthService authService;
    @Mock
    private JwtConfig jwtConfig;
    @Mock
    private TvShowService tvShowService;
    @Mock
    private UserService userService;
    @Mock
    private JwtConfig.TokenProperties refreshTokenProps;
    @Mock
    private JwtConfig.TokenProperties accessTokenProps;
    @Mock
    private JwtConfig.CookieProperties cookieProps;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(jwtConfig.getRefreshToken()).thenReturn(refreshTokenProps);
        when(refreshTokenProps.getDuration()).thenReturn(60L);

        JwtConfig.TokenProperties accessTokenProps = mock(JwtConfig.TokenProperties.class);
        when(jwtConfig.getAccessToken()).thenReturn(accessTokenProps);
        when(accessTokenProps.getDuration()).thenReturn(15L);

        when(jwtConfig.getCookie()).thenReturn(cookieProps);
        when(cookieProps.getDomain()).thenReturn("localhost");
        when(cookieProps.isSecure()).thenReturn(false);
    }

    @Test
    void registerUser_callsAuthService() {
        var input = new RegisterUserInput();
        input.setEmail("email");
        input.setUsername("username");
        input.setPassword("password");
        when(authService.register(input)).thenReturn(true);

        boolean result = userController.registerUser(input);

        assertTrue(result);
        verify(authService).register(input);
    }

    @Test
    void loginUser_setsContextCookiesAndReturnsAccessToken() {
        var input = new LoginUserInput();
        input.setIdentifier("login");
        input.setPassword("password");
        var jwtResult = new LoginResult("refresh-token", "access-token");
        when(authService.login(input)).thenReturn(jwtResult);

        var context = GraphQLContext.newContext().build();
        var response = userController.loginUser(input, context);
        assertEquals("access-token", response.getAccessToken());
        assertTrue(context.hasKey("accessToken"));
        assertTrue(context.hasKey("refreshToken"));
    }

    @Test
    void refreshToken_validToken_setsAccessTokenInContext() {
        var refreshToken = "refresh-token";
        var result = new RefreshResult("new-access-token");
        when(authService.refreshToken(refreshToken)).thenReturn(Optional.of(result));

        var context = GraphQLContext.newContext().build();
        var response = userController.refreshToken(Optional.of(refreshToken), context);

        assertEquals("new-access-token", response.getAccessToken());
        assertTrue(context.hasKey("accessToken"));
    }

    @Test
    void refreshToken_missingToken_returnsNull() {
        var context = GraphQLContext.newContext().build();
        var response = userController.refreshToken(Optional.empty(), context);

        assertNull(response);
    }

    @Test
    void logout_setsDeleteCookiesInContext() {
        var context = GraphQLContext.newContext().build();

        boolean result = userController.logout(context);

        assertTrue(result);
        assertTrue(context.hasKey("accessToken"));
        assertTrue(context.hasKey("refreshToken"));
    }

    @Test
    void saveFavoriteTvShow_callsUserService() {
        var userCtx = new UserContext(userId, Collections.emptyList());
        boolean result = userController.saveFavoriteTvShow(1L, userCtx);

        assertTrue(result);
        verify(userService).saveFavoriteTvShow(1L, userCtx);
    }

    @Test
    void unfavoriteTvShow_callsUserService() {
        var userCtx = new UserContext(userId, Collections.emptyList());
        boolean result = userController.unfavoriteTvShow(1L, userCtx);

        assertTrue(result);
        verify(userService).unfavoriteTvShow(1L, userCtx);
    }

    @Test
    void favoriteTvShows_callsUserService() {
        var input = new FavoriteTvShowsInput(null, null);
        var userCtx = new UserContext(userId, Collections.emptyList());
        Page<UserFavoriteTvShow> page = Page.empty();
        when(userService.getFavoriteShows(input, userCtx)).thenReturn(page);

        Page<UserFavoriteTvShow> result = userController.favoriteTvShows(input, userCtx);

        assertSame(page, result);
    }

    @Test
    void tvShow_batchMapping_resolvesTvShows() {
        var tvShow1 = new TvShowDto(1L, null, null, null, null, null, null, null, null, null, null, null, null, null);
        var tvShow2 = new TvShowDto(2L, null, null, null, null, null, null, null, null, null, null, null, null, null);

        var favorites = List.of(
                new UserFavoriteTvShow(){{
                    setTvShow(new com.upsxace.tv_show_tracker.tv_show.entity.TvShow(){{
                        setId(1L);
                    }});
                }},
                new UserFavoriteTvShow(){{
                    setTvShow(new com.upsxace.tv_show_tracker.tv_show.entity.TvShow(){{
                        setId(2L);
                    }});
                }}
        );

        when(tvShowService.getAllById(List.of(1L,2L))).thenReturn(List.of(tvShow1, tvShow2));

        List<TvShowDto> result = userController.tvShow(favorites);

        assertEquals(2, result.size());
        assertTrue(result.contains(tvShow1));
        assertTrue(result.contains(tvShow2));
    }

    @Test
    void deleteAccount_confirmed_callsServiceAndSetsCookies() {
        var input = new ConfirmInput(true);
        var userCtx = new UserContext(userId, Collections.emptyList());
        var context = GraphQLContext.newContext().build();

        boolean result = userController.deleteAccount(input, userCtx, context);

        assertTrue(result);
        verify(userService).deleteAccount(userCtx);
        assertTrue(context.hasKey("accessToken"));
        assertTrue(context.hasKey("refreshToken"));
    }

    @Test
    void deleteAccount_notConfirmed_returnsFalse() {
        var input = new ConfirmInput(false);
        var userCtx = new UserContext(userId, Collections.emptyList());
        var context = GraphQLContext.newContext().build();

        boolean result = userController.deleteAccount(input, userCtx, context);

        assertFalse(result);
        verify(userService, never()).deleteAccount(any());
    }
}