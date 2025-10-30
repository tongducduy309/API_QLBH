package com.gener.qlbh.dtos.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CustomerCreateReq {
    private String name;

    private String phone;

    private String email;

//    private String company;

    private String taxCode;

    private String address;
}
