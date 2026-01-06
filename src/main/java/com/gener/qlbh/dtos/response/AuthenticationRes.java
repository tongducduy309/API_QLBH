package com.gener.qlbh.dtos.response;

import java.time.LocalDateTime;

public record AuthenticationRes(
        Long id,
        String fullname,
        LocalDateTime createdAt,
        String token
) {
}
