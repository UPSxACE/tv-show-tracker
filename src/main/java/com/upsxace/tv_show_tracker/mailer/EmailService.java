package com.upsxace.tv_show_tracker.mailer;

import com.upsxace.tv_show_tracker.mailer.dto.TvShowRecommendation;
import com.upsxace.tv_show_tracker.user.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for sending emails and recording email sending attempts.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailRepository emailRepository;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * Sends an email using JavaMailSender.
     *
     * @param to      Recipient email address
     * @param subject Email subject
     * @param text    Email body
     * @throws MessagingException if sending fails
     */
    private void sendMail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, text);

        mailSender.send(message);
    }

    /**
     * Sends TV show recommendation emails to a user and records the attempt in the database.
     *
     * @param to      Recipient email address
     * @param subject Email subject
     * @param user    User receiving the recommendations
     * @param tvShows List of recommended TV shows
     */
    @Transactional
    public void sendTvShowRecommendationsEmail(String to, String subject, User user, List<TvShowRecommendation> tvShows) {
        var email = Email.builder()
                .email(to)
                .type("recommendation")
                .user(user)
                .success(false)
                .build();

        emailRepository.save(email);

        try {
            sendMail(to, subject, Templates.recommendationEmail(tvShows));
            email.setSuccess(true);
            email.setConfirmedAt(LocalDateTime.now());
            emailRepository.save(email);
        } catch (Exception e) {
            // Silent fail; email success remains false
        }
    }
}
