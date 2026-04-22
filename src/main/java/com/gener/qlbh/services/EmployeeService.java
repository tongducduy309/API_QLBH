package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.EmployeeCreateReq;
import com.gener.qlbh.dtos.request.EmployeeUpdateReq;
import com.gener.qlbh.dtos.response.EmployeeDetailRes;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.mapper.EmployeeMapper;
import com.gener.qlbh.models.Employee;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.models.User;
import com.gener.qlbh.repositories.EmployeeRepository;
import com.gener.qlbh.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity getAllEmployees() {
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get All Employee Successfully")
                        .data(employeeRepository.findAll())
                        .build()
        );
    }

    @Transactional
    public ResponseEntity getEmployeeById(Long id) throws APIException {
        Employee employee = employeeRepository.findById(id).orElseThrow(() ->
                APIException.builder()
                        .status(ErrorCode.NOT_FOUND.getCode())
                        .message("Cannot Found Employee With Id = " + id)
                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatus())
                        .build()
        );

        EmployeeDetailRes employeeDetailRes = employeeMapper.toEmployeeDetailRes(employee);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get Employee With Id = " + id + " Successfully")
                        .data(employeeDetailRes)
                        .build()
        );
    }

    @Transactional
    public ResponseEntity createEmployee(EmployeeCreateReq req) throws APIException {
        boolean existsEmployeeCode = employeeRepository.existsByCode(req.getCode());
        if (existsEmployeeCode) {
            throw APIException.builder()
                    .status(ErrorCode.CONFLICT.getCode())
                    .message("Employee Code Already Exists")
                    .httpStatusCode(ErrorCode.CONFLICT.getHttpStatus())
                    .build();
        }

        boolean existsUsername = userRepository.existsByUsername(req.getUsername());
        if (existsUsername) {
            throw APIException.builder()
                    .status(ErrorCode.CONFLICT.getCode())
                    .message("Username Already Exists")
                    .httpStatusCode(ErrorCode.CONFLICT.getHttpStatus())
                    .build();
        }

        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .email(req.getEmail())
                .roles(Set.of(req.getRole()))
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        Employee employee = employeeMapper.toEmployee(req);
        employee.setUser(savedUser);
        employee.setActive(true);

        return ResponseEntity.status(SuccessCode.CREATE.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.CREATE.getStatus())
                        .message("Create Employee Successfully")
                        .data(employeeRepository.save(employee))
                        .build()
        );
    }

    @Transactional
    public ResponseEntity updateEmployee(Long id, EmployeeUpdateReq req) throws APIException {
        Employee oldEmployee = employeeRepository.findById(id).orElseThrow(() ->
                APIException.builder()
                        .status(ErrorCode.NOT_FOUND.getCode())
                        .message("Cannot Found Employee With Id = " + id)
                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatus())
                        .build()
        );

        if (!oldEmployee.getCode().equals(req.getCode())
                && employeeRepository.existsByCode(req.getCode())) {
            throw APIException.builder()
                    .status(ErrorCode.CONFLICT.getCode())
                    .message("Employee Code Already Exists")
                    .httpStatusCode(ErrorCode.CONFLICT.getHttpStatus())
                    .build();
        }

        User oldUser = oldEmployee.getUser();
        if (oldUser == null) {
            throw APIException.builder()
                    .status(ErrorCode.NOT_FOUND.getCode())
                    .message("Cannot Found User Of Employee With Id = " + id)
                    .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatus())
                    .build();
        }

        if (!oldUser.getUsername().equals(req.getUsername())
                && userRepository.existsByUsername(req.getUsername())) {
            throw APIException.builder()
                    .status(ErrorCode.CONFLICT.getCode())
                    .message("Username Already Exists")
                    .httpStatusCode(ErrorCode.CONFLICT.getHttpStatus())
                    .build();
        }

        Employee newEmployee = employeeMapper.toEmployee(req);
        newEmployee.setId(id);
        newEmployee.setUser(oldUser);
        newEmployee.setActive(oldEmployee.getActive());
        newEmployee.setCreatedAt(oldEmployee.getCreatedAt());

        oldUser.setUsername(req.getUsername());
        oldUser.setFullName(req.getFullName());
        oldUser.setEmail(req.getEmail());
        oldUser.setRoles(Set.of(req.getRole()));

        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            oldUser.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        userRepository.save(oldUser);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(
                        SuccessCode.REQUEST.getStatus(),
                        "Update Employee Successfully",
                        employeeRepository.save(newEmployee)
                )
        );
    }

    @Transactional
    public ResponseEntity deleteEmployee(Long id) throws APIException {
        Optional<Employee> employeeOptional = employeeRepository.findById(id);

        if (employeeOptional.isEmpty()) {
            throw APIException.builder()
                    .status(ErrorCode.NOT_FOUND.getCode())
                    .message("Cannot Found Employee With Id = " + id)
                    .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatus())
                    .build();
        }

        Employee employee = employeeOptional.get();
        employee.setActive(false);

        if (employee.getUser() != null) {
            employee.getUser().setActive(false);
            userRepository.save(employee.getUser());
        }

        employeeRepository.save(employee);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(
                        SuccessCode.REQUEST.getStatus(),
                        "Delete Employee Successfully",
                        ""
                )
        );
    }

    @Transactional
    public ResponseEntity activateEmployee(Long id) throws APIException {
        Employee employee = employeeRepository.findById(id).orElseThrow(() ->
                APIException.builder()
                        .status(ErrorCode.NOT_FOUND.getCode())
                        .message("Cannot Found Employee With Id = " + id)
                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatus())
                        .build()
        );

        employee.setActive(true);

        if (employee.getUser() != null) {
            employee.getUser().setActive(true);
            userRepository.save(employee.getUser());
        }

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(
                        SuccessCode.REQUEST.getStatus(),
                        "Activate Employee Successfully",
                        employeeRepository.save(employee)
                )
        );
    }
}