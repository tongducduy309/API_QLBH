package com.gener.qlbh.dtos.response;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserProfileRes {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private LocalDateTime createdAt;
}
