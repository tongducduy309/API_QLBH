package com.gener.qlbh.dtos.response;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthProfileRes {
    private Long id;
    private String username;
}
