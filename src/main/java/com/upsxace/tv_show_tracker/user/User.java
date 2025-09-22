package com.upsxace.tv_show_tracker.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter
@Builder @AllArgsConstructor @NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private UUID id;

    @Column
    private String username;

    @Column
    private String password;

    @Column
    private String email;

    @Column
    private String avatarUrl;

    @CreationTimestamp
    @Column
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user")
    private List<UserFavoriteTvShow> favoriteTvShows;

    @Column
    private Boolean emailNotifications;
}
