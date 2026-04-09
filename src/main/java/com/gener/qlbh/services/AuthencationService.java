package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.IntrospectReq;
import com.gener.qlbh.dtos.request.LoginReq;
import com.gener.qlbh.dtos.response.AuthProfileRes;
import com.gener.qlbh.dtos.response.AuthUserRes;
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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
public class AuthencationService {
    private final UserRepository userRepository;
    private final UserService userService;
//    private final AuthencationService authencationService;
    private static final Logger logger = LoggerFactory.getLogger(AuthencationService.class);

    @Value("${jwt.secret}")
    private String SIGNER_KEY;

    @Value("${jwt.expiration-ms}")
    private int EXPIRATION_TOKEN;

    @Transactional
    public ResponseEntity<ResponseObject> introspect(IntrospectReq introspectRequest) throws ParseException, JOSEException, APIException {
//        System.out.println(SIGNER_KEY);
        String token = introspectRequest.getToken();


        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());


        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiredTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        User user = getUserFromToken();

        String tokenNew = generateToken(user);

        boolean value = verified &&expiredTime.after(new Date());
        return ResponseEntity.status(value?SuccessCode.REQUEST.getHttpStatusCode(): ErrorCode.UNAUTHORIZED.getHttpStatusCode()).body(
                new ResponseObject(verified?SuccessCode.REQUEST.getStatus():ErrorCode.UNAUTHORIZED.getStatus(),
                        value?"Token True":"Token False",
                        AuthenticationRes.builder()
                                .user(AuthUserRes.builder()
                                        .fullName(user.getFullName())
                                        .createdAt(user.getCreatedAt())
                                        .build())
                                .accessToken(tokenNew)
                                .active(value)
                                .build()
                        )
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> login(LoginReq loginReq) throws APIException {
        User user = userRepository.findByUsername(loginReq.getUsername()).orElseThrow(()->new APIException(ErrorCode.USERNAME_NOT_EXISTS));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        if (!passwordEncoder.matches(loginReq.getPassword(),user.getPassword())) throw new APIException(ErrorCode.WRONG_PASSWORD);

        String token = generateToken(user);
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
            new ResponseObject(SuccessCode.REQUEST.getStatus(), "Login Successfully",
                    AuthenticationRes.builder()
                            .user(AuthUserRes.builder()
                                    .fullName(user.getFullName())
                                    .createdAt(user.getCreatedAt())
                                    .build())
                            .active(true)
                            .accessToken(token)
                            .build()
                    )
        );

    }



    public String generateToken(User user){
        logger.info(SIGNER_KEY);
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId()+"")
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

    @Transactional
    public User getUserFromToken() throws APIException {
        var context = SecurityContextHolder.getContext();
        String id =  context.getAuthentication().getName();
        return userRepository.findById(Long.parseLong(id)).orElseThrow(()->
                new APIException(ErrorCode.USER_NOT_FOUND));
    }
}
