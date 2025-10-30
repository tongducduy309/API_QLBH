package com.gener.qlbh.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=120)
    private String username;

    @Column(nullable=false)
    private String passwordHash; // BCrypt

    @Column(nullable=false)
    private String fullName;

    private String email;
    private Boolean enabled = true;
}