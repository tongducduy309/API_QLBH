package com.gener.qlbh.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthTokenReq {
    @NotBlank String token;
}
