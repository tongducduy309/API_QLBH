package com.gener.qlbh.services;

import com.gener.qlbh.dtos.response.AuthProfileRes;
import com.gener.qlbh.dtos.response.UserProfileRes;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.Role;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.models.User;
import com.gener.qlbh.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public ResponseEntity<ResponseObject> getAllUsers(){
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get All Users Successfully")
                        .data(userRepository.findAll())
                        .build()
        );

    }

    @Transactional
    public ResponseEntity<ResponseObject> getProfile() throws APIException {
        var context = SecurityContextHolder.getContext();
        String name =  context.getAuthentication().getName();
        User user = userRepository.findByUsername(name).orElseThrow(()->
                new APIException(ErrorCode.USER_NOT_FOUND));
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Profile")
                        .data(UserProfileRes.builder()
                                .id(user.getId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .fullName(user.getFullName())
                                .build())
                        .build()


        );
    }

    public Set<Role> getCurrentUserRoles() {
        return Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                .getAuthorities()
                .stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }

    public boolean hasAnyRole(Set<Role> roles, Role... allowedRoles) {
        return Arrays.stream(allowedRoles)
                .anyMatch(roles::contains);
    }

    public boolean hasOnlyRole(Set<Role> roles, Role role) {
        return roles.size() == 1 && roles.contains(role);
    }


}


