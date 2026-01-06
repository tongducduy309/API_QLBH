package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.AuthenticationReq;
import com.gener.qlbh.dtos.request.IntrospectReq;
import com.gener.qlbh.dtos.response.AuthProfileRes;
import com.gener.qlbh.dtos.response.AuthenticationRes;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.models.User;
import com.gener.qlbh.repositories.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;

@Service
public class AuthencationService {
    @Autowired
    private UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthencationService.class);

    @Value("${jwt.secret}")
    private String SIGNER_KEY;

    @Value("${jwt.expiration-ms}")
    private int EXPIRATION_TOKEN;

    @Transactional
    public ResponseEntity<ResponseObject> introspect(IntrospectReq introspectRequest) throws ParseException, JOSEException {
//        System.out.println(SIGNER_KEY);
        String token = introspectRequest.getToken();


        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());


        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiredTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        boolean value = verified &&expiredTime.after(new Date());
        return ResponseEntity.status(value? HttpStatus.OK:HttpStatus.BAD_REQUEST).body(
                new ResponseObject(verified?200:401,value?"Token True":"Token False",value)
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> authenticate(AuthenticationReq authenticationRequest) throws APIException {
        logger.info(authenticationRequest.getUsername());
        User user = userRepository.findByUsername(authenticationRequest.getUsername()).orElseThrow(()->new APIException(ErrorCode.USERNAME_NOT_EXISTS));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        if (!passwordEncoder.matches(authenticationRequest.getPassword(),user.getPassword())) throw new APIException(ErrorCode.WRONG_PASSWORD);

        String token = generateToken(user);
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
            new ResponseObject(SuccessCode.REQUEST.getStatus(), "Login Successfully",new AuthenticationRes(
                    user.getId(),
                    "",
                    LocalDateTime.now(),
                    token
            ))
        );

    }



    public String generateToken(User user){
        logger.info(SIGNER_KEY);
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("qlbh.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus((EXPIRATION_TOKEN), ChronoUnit.HOURS).toEpochMilli()
                ))
                .claim("scope",buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header,payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return  jwsObject.serialize();
        } catch (JOSEException e) {
            logger.error("Cannot Create Token",e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().forEach(stringJoiner::add);
        }
        return stringJoiner.toString();
    }

    @Transactional
    public ResponseEntity<ResponseObject> getProfile() throws APIException {
        var context = SecurityContextHolder.getContext();
        String name =  context.getAuthentication().getName();
        User user = userRepository.findByUsername(name).orElseThrow(()->
                new APIException(ErrorCode.USER_NOT_EXISTS));
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Authenticated")
                        .data(AuthProfileRes.builder()
                                .id(user.getId())
                                .username(user.getUsername())
                                .build())
                        .build()


        );
    }
}
