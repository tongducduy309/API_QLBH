package com.gener.qlbh.controllers;

import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping(path = "/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    ResponseEntity<ResponseObject> getMe() throws APIException {
        return userService.getProfile();
    }

    @GetMapping
    ResponseEntity<ResponseObject> getAllUsers(){
        return userService.getAllUsers();
    }
}
