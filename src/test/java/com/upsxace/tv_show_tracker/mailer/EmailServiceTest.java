package com.upsxace.tv_show_tracker.mailer;

import com.upsxace.tv_show_tracker.mailer.dto.TvShowRecommendation;
import com.upsxace.tv_show_tracker.user.entity.User;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceTest {
    @InjectMocks
    private EmailService emailService;

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private EmailRepository emailRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(emailService, "from", "test@example.com");
    }

    @Test
    void sendTvShowRecommendationsEmail_successfulEmail_setsSuccess() throws Exception {
        User user = new User();
        List<TvShowRecommendation> tvShows = List.of();
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendTvShowRecommendationsEmail("to@example.com", "Subject", user, tvShows);

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(emailRepository, times(2)).save(emailCaptor.capture());
        Email savedEmail = emailCaptor.getValue();
        assertTrue(savedEmail.getSuccess());
    }

    @Test
    void sendTvShowRecommendationsEmail_mailFails_keepsSuccessFalse() throws Exception {
        User user = new User();
        List<TvShowRecommendation> tvShows = List.of();
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(RuntimeException.class).when(mailSender).send(any(MimeMessage.class));

        emailService.sendTvShowRecommendationsEmail("to@example.com", "Subject", user, tvShows);

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(emailRepository, times(1)).save(emailCaptor.capture());
        Email savedEmail = emailCaptor.getValue();
         assertFalse(savedEmail.getSuccess());
    }
}