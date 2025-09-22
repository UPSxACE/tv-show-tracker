package com.upsxace.tv_show_tracker.experience;

import com.upsxace.tv_show_tracker.mailer.EmailRepository;
import com.upsxace.tv_show_tracker.mailer.EmailService;
import com.upsxace.tv_show_tracker.mailer.dto.TvShowRecommendation;
import com.upsxace.tv_show_tracker.tv_show.repository.TvShowRepository;
import com.upsxace.tv_show_tracker.user.repository.UserFavoriteTvShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service responsible for handling user experience logic, such as reacting to favorite TV shows
 * and sending personalized recommendations via email.
 */
@Component
@RequiredArgsConstructor
public class ExperienceService {

    private final UserFavoriteTvShowRepository userFavoriteTvShowRepository;
    private final EmailRepository emailRepository;
    private final TvShowRepository tvShowRepository;
    private final EmailService emailService;

    private LocalDateTime lastExecution = LocalDateTime.now();

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Reacts to a user favoriting a TV show. If conditions are met, sends a recommendation email
     * with 3 recommended TV shows of similar genres. Ensures emails are sent at most once every 250ms
     * and with a cooldown of 2 days between recommendation emails.
     *
     * @param userId The ID of the user who favorited a TV show
     */
    @Async
    @Transactional
    public void reactToFavorite(UUID userId) {
        // Rate limiting: execute at most once every 250ms
        if (LocalDateTime.now().isBefore(lastExecution.plus(Duration.ofMillis(250)))) {
            return;
        }
        lastExecution = LocalDateTime.now();

        var lastEmail = emailRepository.findByUserIdAndTypeOrderBySentAtDesc(userId, "recommendation");
        var userFavorites = userFavoriteTvShowRepository.findFirst3ByUserIdOrderByFavoritedAtDesc(userId); // ordered by latest favorites

        boolean cooldownOver = lastEmail.isEmpty() ||
                LocalDateTime.now().isAfter(lastEmail.get().getSentAt().plus(Duration.ofDays(2)));

        // Only send recommendations if cooldown is over and user has more than 3 favorite shows
        if (!cooldownOver || userFavorites.size() != 3) {
            return;
        }

        var genreIds = userFavorites.stream()
                .flatMap(fav -> fav.getTvShow().getTvShowGenres().stream()
                        .map(tg -> tg.getGenre().getId()))
                .toList();

        var shows = tvShowRepository.findRecommendations(userId, genreIds, PageRequest.of(0, 3));
        var recommendations = shows.stream()
                .map(x -> new TvShowRecommendation(
                        x.getName(),
                        x.getPosterUrl(),
                        frontendUrl + "/tv-shows/" + x.getId()
                ))
                .toList();

        if (recommendations.size() < 3) {
            return;
        }

        emailService.sendTvShowRecommendationsEmail(
                userFavorites.getFirst().getUser().getEmail(),
                "Recommendations",
                userFavorites.getFirst().getUser(),
                recommendations
        );
    }
}
