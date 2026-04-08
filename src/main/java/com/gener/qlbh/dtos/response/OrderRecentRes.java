package com.gener.qlbh.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class OrderRecentRes {
    private Long id;
    private String code;
    private String customerName;
    private Double total;
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate createdAt;
}
