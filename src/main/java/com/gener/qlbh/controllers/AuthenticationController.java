package com.gener.qlbh.controllers;

import com.gener.qlbh.dtos.request.AuthTokenReq;
import com.gener.qlbh.dtos.request.AuthenticationReq;
import com.gener.qlbh.dtos.request.IntrospectReq;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.services.AuthencationService;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthencationService authencationService;

    @PostMapping("/authenticate")
    ResponseEntity<ResponseObject> authenticate(@RequestBody AuthenticationReq authenticationRequest) throws APIException {
        return authencationService.authenticate(authenticationRequest);
    }
    @PostMapping("/introspect")
    ResponseEntity<ResponseObject> introspect(@RequestBody IntrospectReq introspectRequest) throws ParseException, JOSEException {
        return authencationService.introspect(introspectRequest);
    }
    @GetMapping("/me")
    ResponseEntity<ResponseObject> getMe() throws APIException {
        return authencationService.getProfile();
    }
}
