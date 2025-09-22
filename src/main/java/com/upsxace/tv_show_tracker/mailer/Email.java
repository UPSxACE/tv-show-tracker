package com.upsxace.tv_show_tracker.mailer;

import com.upsxace.tv_show_tracker.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "emails")
@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String type;

    @Column
    private String email;

    @Column
    private Boolean success;

    @CreationTimestamp
    @Column
    private LocalDateTime sentAt;

    @Column
    private LocalDateTime confirmedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
