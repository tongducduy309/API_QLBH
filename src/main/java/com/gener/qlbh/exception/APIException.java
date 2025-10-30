package com.gener.qlbh.exception;

import com.gener.qlbh.enums.ErrorCode;
import lombok.*;
import org.springframework.http.HttpStatusCode;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class APIException extends Exception{
    private int status;
    private String message;
    private HttpStatusCode httpStatusCode;

    public APIException(ErrorCode errorCode){
        this.status = errorCode.getStatus();
        this.message = errorCode.getMessage();
        this.httpStatusCode = errorCode.getHttpStatusCode();

    }
}
