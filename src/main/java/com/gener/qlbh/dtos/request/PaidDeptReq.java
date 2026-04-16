package com.gener.qlbh.dtos.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PaidDeptReq {
    private Long orderId;
    private Double amount;

}
