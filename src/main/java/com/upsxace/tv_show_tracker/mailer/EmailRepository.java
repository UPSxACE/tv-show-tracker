package com.upsxace.tv_show_tracker.mailer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailRepository extends JpaRepository<Email, Long> {
    Optional<Email> findByUserIdAndTypeOrderBySentAtDesc(UUID userId, String type);
    void deleteByUserId(UUID id);
}
