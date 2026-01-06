package com.gener.qlbh.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum ErrorCode {
    USERNAME_NOT_EXISTS(404,"Tài khoản không tồn tại", HttpStatus.NOT_FOUND),

    USER_NOT_EXISTS(404,"Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    EMAIL_EXISTS(409,"Email Already Taken", HttpStatus.CONFLICT),
    UNAUTHORIZED(401,"Authentication Required", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403,"Access Denied. You Do Not Have Permission To Access This Resource.", HttpStatus.FORBIDDEN),

    BAD_REQUEST(400,"Not Found", HttpStatus.BAD_REQUEST),
    NOT_FOUND(404,"Not Found", HttpStatus.NOT_FOUND),
    CONFLICT(409,"Conflict", HttpStatus.CONFLICT),
    INTERNAL_SERVER_ERROR(500,"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),

    UNPROCESSABLE_ENTITY(422,"Unprocessable Content",HttpStatus.UNPROCESSABLE_ENTITY),
    WRONG_PASSWORD(404,"Tài khoản hoặc mật khẩu chưa chính xác", HttpStatus.NOT_FOUND),

    CANNOT_READ_IMAGE(500,"Cannot Read Image", HttpStatus.INTERNAL_SERVER_ERROR)
    ;
    private int status;
    private String message;
    private HttpStatusCode httpStatusCode;
}
