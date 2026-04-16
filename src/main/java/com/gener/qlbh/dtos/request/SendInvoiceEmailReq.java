package com.gener.qlbh.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendInvoiceEmailReq {
    private String to;
    private String subject;
    private String content;
}