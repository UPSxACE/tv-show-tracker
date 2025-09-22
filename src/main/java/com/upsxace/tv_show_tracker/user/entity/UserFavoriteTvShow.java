package com.upsxace.tv_show_tracker.user.entity;

import com.upsxace.tv_show_tracker.tv_show.entity.TvShow;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_favorite_tv_shows")
@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
public class UserFavoriteTvShow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "tv_show_id")
    private TvShow tvShow;

    @Column
    @CreationTimestamp
    private LocalDateTime favoritedAt;
}
