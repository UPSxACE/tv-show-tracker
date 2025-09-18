package com.upsxace.tv_show_tracker.common.app_property;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_properties")
@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
public class AppProperty {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String key;
    @Column
    private String value;
}
