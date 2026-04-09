package com.gener.qlbh.controllers;

import com.gener.qlbh.dtos.request.UserSettingUpdateReq;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.services.UserService;
import com.gener.qlbh.services.UserSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping(path = "/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserSettingService userSettingService;

    @GetMapping("/me")
    ResponseEntity<ResponseObject> getMe() throws APIException {
        return userService.getProfile();
    }

    @GetMapping
    ResponseEntity<ResponseObject> getAllUsers(){
        return userService.getAllUsers();
    }

    @GetMapping("/me/settings")
    ResponseEntity<ResponseObject> getMySettings() throws APIException {
        return userSettingService.getMySettings();
    }

    @PutMapping("/me/settings")
    ResponseEntity<ResponseObject> updateMySettings(
            @Valid @RequestBody UserSettingUpdateReq req
    ) throws APIException {
        return userSettingService.updateMySettings(req);
    }
}
