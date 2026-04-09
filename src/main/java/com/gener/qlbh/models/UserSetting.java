package com.gener.qlbh.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String appName;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String appIcon;

    @Builder.Default
    @Column(nullable = false)
    private Boolean emailNotify = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean desktopNotify = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
