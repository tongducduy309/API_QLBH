package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.EmployeeCreateReq;
import com.gener.qlbh.dtos.request.EmployeeLeaveMarkReq;
import com.gener.qlbh.dtos.request.EmployeeUpdateReq;
import com.gener.qlbh.dtos.response.EmployeeDetailRes;
import com.gener.qlbh.dtos.response.EmployeeLeaveRes;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.mapper.EmployeeLeaveMapper;
import com.gener.qlbh.mapper.EmployeeMapper;
import com.gener.qlbh.models.*;
import com.gener.qlbh.repositories.EmployeeCodeSequenceRepository;
import com.gener.qlbh.repositories.EmployeeLeaveRepository;
import com.gener.qlbh.repositories.EmployeeRepository;
import com.gener.qlbh.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeLeaveRepository employeeLeaveRepository;
    private final UserRepository userRepository;
    private final EmployeeMapper employeeMapper;
    private final EmployeeLeaveMapper employeeLeaveMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeCodeSequenceRepository employeeCodeSequenceRepository;

    @Value("${user.password_default}")
    private String PASSWORD_DEFAULT;

    private Employee findEmployeeOrThrow(Long id) throws APIException {
        return employeeRepository.findById(id).orElseThrow(() ->
                APIException.builder()
                        .status(ErrorCode.NOT_FOUND.getCode())
                        .message("Cannot Found Employee With Id = " + id)
                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatus())
                        .build()
        );
    }

    private EmployeeDetailRes buildEmployeeDetail(Employee employee) {
        EmployeeDetailRes employeeDetailRes = employeeMapper.toEmployeeDetailRes(employee);

        LocalDate now = LocalDate.now();

        List<EmployeeLeaveRes> leaves = employeeLeaveRepository
                .findByEmployeeIdAndLeaveDateBetween(
                        employee.getId(),
                        now.minusMonths(1),
                        now.plusMonths(1)
                )
                .stream()
                .map(employeeLeaveMapper::toRes)
                .toList();

        Double leaveDaysThisMonth = employeeLeaveRepository.sumLeaveDays(
                employee.getId(),
                now.getMonthValue(),
                now.getYear()
        );

        Boolean onLeaveToday = employeeLeaveRepository.existsByEmployeeIdAndLeaveDate(
                employee.getId(),
                now
        );

        employeeDetailRes.setLeaves(leaves);
        employeeDetailRes.setLeaveDaysThisMonth(leaveDaysThisMonth != null ? leaveDaysThisMonth : 0D);
        employeeDetailRes.setOnLeaveToday(onLeaveToday);

        return employeeDetailRes;
    }

    public ResponseEntity<?> getAllEmployees() {
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get All Employee Successfully")
                        .data(employeeRepository.findAll())
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<?> getEmployeeById(Long id) throws APIException {
        Employee employee = findEmployeeOrThrow(id);

        EmployeeDetailRes employeeDetailRes = buildEmployeeDetail(employee);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get Employee With Id = " + id + " Successfully")
                        .data(employeeDetailRes)
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<?> createEmployee(EmployeeCreateReq req) throws APIException {
//        boolean existsEmployeeCode = employeeRepository.existsByCode(req.getCode());
//        if (existsEmployeeCode) {
//            throw APIException.builder()
//                    .status(ErrorCode.CONFLICT.getCode())
//                    .message("Employee Code Already Exists")
//                    .httpStatusCode(ErrorCode.CONFLICT.getHttpStatus())
//                    .build();
//        }

        boolean existsUsername = userRepository.existsByUsername(req.getUsername());
        if (existsUsername) {
            throw APIException.builder()
                    .status(ErrorCode.CONFLICT.getCode())
                    .message("Username Already Exists")
                    .httpStatusCode(ErrorCode.CONFLICT.getHttpStatus())
                    .build();
        }

        log.info("Password: "+ PASSWORD_DEFAULT);

        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(PASSWORD_DEFAULT))
                .fullName(req.getFullName())
                .email(req.getEmail())
                .roles(Set.of(req.getRole()))
                .active(true)
                .build();

        User savedUser = userRepository.save(user);


        Employee employee = employeeMapper.toEmployee(req);
        employee.setUser(savedUser);
        String code = generateEmployeeCode();

        employee.setCode(code);
        employee.setActive(true);


        Employee savedEmployee = employeeRepository.save(employee);

        return ResponseEntity.status(SuccessCode.CREATE.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.CREATE.getStatus())
                        .message("Create Employee Successfully")
                        .data(savedEmployee)
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<?> updateEmployee(Long id, EmployeeUpdateReq req) throws APIException {
        Employee oldEmployee = findEmployeeOrThrow(id);

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

        Employee savedEmployee = employeeRepository.save(newEmployee);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Update Employee Successfully")
                        .data(savedEmployee)
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<?> deleteEmployee(Long id) throws APIException {
        Employee employee = findEmployeeOrThrow(id);
        employee.setActive(false);

        if (employee.getUser() != null) {
            employee.getUser().setActive(false);
            userRepository.save(employee.getUser());
        }

        employeeRepository.save(employee);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Delete Employee Successfully")
                        .data("")
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<?> activateEmployee(Long id) throws APIException {
        Employee employee = findEmployeeOrThrow(id);
        employee.setActive(true);

        if (employee.getUser() != null) {
            employee.getUser().setActive(true);
            userRepository.save(employee.getUser());
        }

        Employee savedEmployee = employeeRepository.save(employee);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Activate Employee Successfully")
                        .data(savedEmployee)
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<?> getEmployeeLeaves(Long id, LocalDate from, LocalDate to) throws APIException {
        findEmployeeOrThrow(id);

        LocalDate start = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate end = to != null ? to : start.withDayOfMonth(start.lengthOfMonth());

        List<EmployeeLeaveRes> leaves = employeeLeaveRepository
                .findByEmployeeIdAndLeaveDateBetween(id, start, end)
                .stream()
                .map(employeeLeaveMapper::toRes)
                .toList();

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get Employee Leaves Successfully")
                        .data(leaves)
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<?> markEmployeeLeave(Long id, EmployeeLeaveMarkReq req) throws APIException {
        Employee employee = findEmployeeOrThrow(id);

        if (req.getLeaveDate() == null) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getCode())
                    .message("Leave Date Is Required")
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatus())
                    .build();
        }

        if (req.getLeaveType() == null) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getCode())
                    .message("Leave Type Is Required")
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatus())
                    .build();
        }

        boolean existsLeave = employeeLeaveRepository.existsByEmployeeIdAndLeaveDate(
                id,
                req.getLeaveDate()
        );

        if (existsLeave) {
            throw APIException.builder()
                    .status(ErrorCode.CONFLICT.getCode())
                    .message("Employee Leave Already Exists On This Date")
                    .httpStatusCode(ErrorCode.CONFLICT.getHttpStatus())
                    .build();
        }

        EmployeeLeave employeeLeave = EmployeeLeave.builder()
                .employee(employee)
                .leaveDate(req.getLeaveDate())
                .leaveType(req.getLeaveType())
                .reason(req.getReason())
                .build();

        EmployeeLeave savedLeave = employeeLeaveRepository.save(employeeLeave);

        return ResponseEntity.status(SuccessCode.CREATE.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.CREATE.getStatus())
                        .message("Mark Employee Leave Successfully")
                        .data(employeeLeaveMapper.toRes(savedLeave))
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<?> deleteEmployeeLeave(Long id, LocalDate leaveDate) throws APIException {
        findEmployeeOrThrow(id);

        EmployeeLeave employeeLeave = employeeLeaveRepository
                .findByEmployeeIdAndLeaveDate(id, leaveDate)
                .orElseThrow(() ->
                        APIException.builder()
                                .status(ErrorCode.NOT_FOUND.getCode())
                                .message("Cannot Found Employee Leave On Date = " + leaveDate)
                                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatus())
                                .build()
                );

        employeeLeaveRepository.delete(employeeLeave);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Delete Employee Leave Successfully")
                        .data("")
                        .build()
        );
    }

    private String generateEmployeeCode() {
        EmployeeCodeSequence sequence = employeeCodeSequenceRepository.save(
                new EmployeeCodeSequence()
        );

        return String.format("NV%05d", sequence.getId());
    }


}