package com.gener.qlbh.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@AllArgsConstructor

@NoArgsConstructor
@Getter
public enum SuccessCode {
    REQUEST(200, HttpStatus.OK),
    CREATE(201, HttpStatus.CREATED),
    NO_CONTENT(204, HttpStatus.NO_CONTENT),
    ;
    private int status;
    private HttpStatusCode httpStatusCode;
}
