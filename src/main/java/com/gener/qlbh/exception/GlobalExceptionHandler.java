package com.gener.qlbh.exception;

import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.models.ResponseObject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {
//    @ExceptionHandler(value= APIException.class)
//    ResponseEntity<ResponseObject> handlingAPIException(APIException exception){
//        return ResponseEntity.status(exception.getHttpStatusCode()).body(
//                ResponseObject.builder()
//                        .status(exception.getStatus())
//                        .message(exception.getMessage())
//                        .build()
//        );
//    }
//
//    @ExceptionHandler(value= IllegalArgumentException.class)
//    ResponseEntity<ResponseObject> handlingIllegalArgumentException(IllegalArgumentException exception){
//        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatusCode()).body(
//                ResponseObject.builder()
//                        .status(ErrorCode.BAD_REQUEST.getStatus())
//                        .message(exception.getMessage())
//                        .build()
//        );
//    }
//
//    @ExceptionHandler(value = MethodArgumentNotValidException.class)
//    ResponseEntity<ResponseObject> handlingValidation(MethodArgumentNotValidException exception){
//        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatusCode()).body(
//                ResponseObject.builder()
//                        .status(ErrorCode.BAD_REQUEST.getStatus())
//                        .message(Objects.requireNonNull(exception.getFieldError()).getDefaultMessage())
//                        .build()
//        );
//    }
//
////    @ExceptionHandler(value = ConstraintViolationException.class)
////    ResponseEntity<ResponseObject> handlingConstraintViolationException(ConstraintViolationException exception){
////        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatusCode()).body(
////                ResponseObject.builder()
////                        .status(ErrorCode.BAD_REQUEST.getStatus())
////                        .message(exception.getMessage())
////                        .build()
////        );
////    }
//
//    @ExceptionHandler(DataIntegrityViolationException.class)
//    ResponseEntity<ResponseObject> handleDuplicateKey(DataIntegrityViolationException ex) {
//        String message = "Data integrity violation (maybe duplicate key or unique constraint)";
//
//        Throwable cause = ex.getRootCause();
//        if (cause != null && cause.getMessage() != null) {
//            String causeMessage = cause.getMessage().toLowerCase();
//            if (causeMessage.contains("unique") || causeMessage.contains("duplicate")) {
//                message = "Value already exists (duplicate unique key)";
//            } else if (causeMessage.contains("not-null")) {
//                message = "Required field cannot be null";
//            } else if (causeMessage.contains("foreign key")) {
//                message = "Invalid reference (foreign key constraint failed)";
//            }
//        }
//
//        return ResponseEntity.status(ErrorCode.CONFLICT.getHttpStatusCode()).body(
//                ResponseObject.builder()
//                        .status(ErrorCode.CONFLICT.getStatus())
//                        .message(message)
//                        .build()
//        );
//    }



}
