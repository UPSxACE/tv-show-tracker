package com.upsxace.tv_show_tracker.experience;

import com.upsxace.tv_show_tracker.mailer.EmailService;
import com.upsxace.tv_show_tracker.mailer.EmailRepository;
import com.upsxace.tv_show_tracker.mailer.Email;
import com.upsxace.tv_show_tracker.tv_show.entity.TvShow;
import com.upsxace.tv_show_tracker.tv_show.repository.TvShowRepository;
import com.upsxace.tv_show_tracker.user.entity.User;
import com.upsxace.tv_show_tracker.user.entity.UserFavoriteTvShow;
import com.upsxace.tv_show_tracker.user.repository.UserFavoriteTvShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

class ExperienceServiceTest {

    @InjectMocks
    private ExperienceService experienceService;

    @Mock
    private UserFavoriteTvShowRepository userFavoriteTvShowRepository;
    @Mock
    private EmailRepository emailRepository;
    @Mock
    private TvShowRepository tvShowRepository;
    @Mock
    private EmailService emailService;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void reactToFavorite_cooldownNotOver_doesNotSendEmail() throws InterruptedException {
        when(emailRepository.findByUserIdAndTypeOrderBySentAtDesc(userId, "recommendation"))
                .thenReturn(Optional.of(new Email(){{
                    setSentAt(LocalDateTime.now());
                }}));

        experienceService.reactToFavoriteSync(userId);

        Thread.sleep(500);
        verify(emailService, never()).sendTvShowRecommendationsEmail(any(), any(), any(), any());
    }

    @Test
    void reactToFavorite_lessThanThreeFavorites_doesNotSendEmail() throws InterruptedException {
        when(emailRepository.findByUserIdAndTypeOrderBySentAtDesc(userId, "recommendation"))
                .thenReturn(Optional.empty());

        when(userFavoriteTvShowRepository.findFirst3ByUserIdOrderByFavoritedAtDesc(userId))
                .thenReturn(List.of(new UserFavoriteTvShow(), new UserFavoriteTvShow())); // only 2 favorites

        experienceService.reactToFavoriteSync(userId);

        Thread.sleep(500);
        verify(emailService, never()).sendTvShowRecommendationsEmail(any(), any(), any(), any());
    }

    @Test
    void reactToFavorite_sendsEmail_whenCriteriaMet() throws InterruptedException {
        User user = new User();
        user.setEmail("test@example.com");

        var favorites = List.of(
                new UserFavoriteTvShow() {{ setUser(user); setTvShow(mock(TvShow.class)); }},
                new UserFavoriteTvShow() {{ setUser(user); setTvShow(mock(TvShow.class)); }},
                new UserFavoriteTvShow() {{ setUser(user); setTvShow(mock(TvShow.class)); }}
        );

        when(emailRepository.findByUserIdAndTypeOrderBySentAtDesc(userId, "recommendation"))
                .thenReturn(Optional.empty());
        when(userFavoriteTvShowRepository.findFirst3ByUserIdOrderByFavoritedAtDesc(userId))
                .thenReturn(favorites);
        when(tvShowRepository.findRecommendations(eq(userId), anyList(), any(PageRequest.class)))
                .thenReturn(List.of(mock(TvShow.class), mock(TvShow.class), mock(TvShow.class)));

        experienceService.reactToFavoriteSync(userId);

        Thread.sleep(500);
        verify(emailService).sendTvShowRecommendationsEmail(
                eq("test@example.com"),
                eq("Recommendations"),
                eq(user),
                ArgumentMatchers.anyList()
        );
    }
}
