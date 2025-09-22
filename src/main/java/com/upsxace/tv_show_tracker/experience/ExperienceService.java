package com.upsxace.tv_show_tracker.experience;

import com.upsxace.tv_show_tracker.mailer.EmailRepository;
import com.upsxace.tv_show_tracker.mailer.EmailService;
import com.upsxace.tv_show_tracker.mailer.dto.TvShowRecommendation;
import com.upsxace.tv_show_tracker.tv_show.TvShowRepository;
import com.upsxace.tv_show_tracker.user.UserFavoriteTvShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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



    @Async
    @Transactional
    public void reactToFavorite(UUID userId){
        if(LocalDateTime.now().isBefore(lastExecution.plus(Duration.ofMillis(250)))){
            // execute at most once each 250ms
            return;
        }
        lastExecution = LocalDateTime.now();

        var lastEmail = emailRepository.findByUserIdAndTypeOrderBySentAtDesc(userId, "recommendation");
        var userFavorites = userFavoriteTvShowRepository.findFirst3ByUserIdOrderByFavoritedAtDesc(userId);

        var cooldownOver = lastEmail.isEmpty() || LocalDateTime.now().isAfter(lastEmail.get().getSentAt().plus(Duration.ofDays(2)));

        if(!cooldownOver || userFavorites.size() != 3){
            return;
        }

        var genreIds = userFavorites
                .stream()
                .map(c ->
                        c.getTvShow().getTvShowGenres()
                                .stream().map(tg -> tg.getGenre().getId())
                                .toList()
                )
                .flatMap(List::stream)
                .toList();

        var shows = tvShowRepository.findRecommendations(userId, genreIds, PageRequest.of(0,3));
        var recommendations = shows.stream().map(x -> new TvShowRecommendation(
                x.getName(),
                x.getPosterUrl(),
                frontendUrl + "/tv-shows/" + x.getId()
        )).toList();

        if(recommendations.size() < 3)
            return;

        emailService.sendTvShowRecommendationsEmail(
                userFavorites.getFirst().getUser().getEmail(),
                "Recommendations",
                userFavorites.getFirst().getUser(),
                recommendations
        );

    }
}
