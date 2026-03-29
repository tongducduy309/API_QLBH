package com.gener.qlbh.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class CustomerDetailRes {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String taxCode;
    private String address;
    private LocalDateTime createdAt;
    private Double point;
}
