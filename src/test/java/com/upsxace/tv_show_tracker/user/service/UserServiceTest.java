package com.upsxace.tv_show_tracker.user.service;

import com.upsxace.tv_show_tracker.common.exceptions.NotFoundException;
import com.upsxace.tv_show_tracker.common.graphql.PageInput;
import com.upsxace.tv_show_tracker.common.graphql.SortDirection;
import com.upsxace.tv_show_tracker.common.jwt.UserContext;
import com.upsxace.tv_show_tracker.experience.ExperienceService;
import com.upsxace.tv_show_tracker.mailer.EmailRepository;
import com.upsxace.tv_show_tracker.tv_show.entity.TvShow;
import com.upsxace.tv_show_tracker.tv_show.repository.TvShowRepository;
import com.upsxace.tv_show_tracker.user.entity.User;
import com.upsxace.tv_show_tracker.user.entity.UserFavoriteTvShow;
import com.upsxace.tv_show_tracker.user.graphql.FavoriteTvShowsInput;
import com.upsxace.tv_show_tracker.user.graphql.UserFavoriteTvShowOrderInput;
import com.upsxace.tv_show_tracker.user.graphql.UserFavoriteTvShowSortableField;
import com.upsxace.tv_show_tracker.user.repository.UserFavoriteTvShowRepository;
import com.upsxace.tv_show_tracker.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private TvShowRepository tvShowRepository;
    @Mock
    private UserFavoriteTvShowRepository userFavoriteTvShowRepository;
    @Mock
    private ExperienceService experienceService;
    @Mock
    private EmailRepository emailRepository;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername_existingUser_returnsUserDetails() {
        var user = User.builder().id(userId).password("pass").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var userDetails = userService.loadUserByUsername(userId.toString());

        assertNotNull(userDetails);
        assertEquals(userId.toString(), userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_nonExistingUser_throwsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> userService.loadUserByUsername(userId.toString()));
    }

    @Test
    void saveFavoriteTvShow_existingTvShow_callsRepositories() {
        var tvShow = new TvShow();
        when(tvShowRepository.findById(1L)).thenReturn(Optional.of(tvShow));
        when(userFavoriteTvShowRepository.findOne(any())).thenReturn(Optional.empty());

        userService.saveFavoriteTvShow(1L, new UserContext(userId, Collections.emptyList()));

        verify(userFavoriteTvShowRepository).save(any(UserFavoriteTvShow.class));
        verify(experienceService).reactToFavorite(userId);
    }

    @Test
    void saveFavoriteTvShow_tvShowNotFound_throwsNotFoundException() {
        when(tvShowRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.saveFavoriteTvShow(1L, new UserContext(userId, Collections.emptyList())));
    }

    @Test
    void deleteAccount_callsRepositories() {
        var userCtx = new UserContext(userId, Collections.emptyList());

        userService.deleteAccount(userCtx);

        verify(emailRepository).deleteByUserId(userId);
        verify(userFavoriteTvShowRepository).deleteByUserId(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void getFavoriteShows_callsRepository() {
        UserFavoriteTvShowSortableField field = UserFavoriteTvShowSortableField.favoritedAt;
        SortDirection direction = SortDirection.DESC;

        PageInput page = new PageInput(0, 20);
        UserFavoriteTvShowOrderInput order = new UserFavoriteTvShowOrderInput(field,direction);

        var input = new FavoriteTvShowsInput(page, order);

        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.valueOf(direction.name()), field.name()));
        when(userFavoriteTvShowRepository.findAllByUserId(pageable, userId))
                .thenReturn(Page.empty());

        Page<UserFavoriteTvShow> result = userService.getFavoriteShows(input, new UserContext(userId, Collections.emptyList()));

        assertNotNull(result);
    }
}
