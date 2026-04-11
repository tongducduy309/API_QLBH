package com.gener.qlbh.models;

import com.gener.qlbh.entities.PrintOptions;
import com.gener.qlbh.enums.PageOrientation;
import com.gener.qlbh.enums.PaperSize;
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


    @Builder.Default
    @Column(nullable = false)
    private Boolean emailNotify = true;

    private PaperSize paperSize;
    private PageOrientation pageOrientation;
    private String deviceName;
    private Integer copies;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
