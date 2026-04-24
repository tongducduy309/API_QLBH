package com.gener.qlbh.controllers;

import com.gener.qlbh.dtos.request.EmployeeCreateReq;
import com.gener.qlbh.dtos.request.EmployeeLeaveMarkReq;
import com.gener.qlbh.dtos.request.EmployeeUpdateReq;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.services.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<?> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) throws APIException {
        return employeeService.getEmployeeById(id);
    }

    @PostMapping
    public ResponseEntity<?> createEmployee(@RequestBody @Valid EmployeeCreateReq req) throws APIException {
        return employeeService.createEmployee(req);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(
            @PathVariable Long id,
            @RequestBody @Valid EmployeeUpdateReq req
    ) throws APIException {
        return employeeService.updateEmployee(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) throws APIException {
        return employeeService.deleteEmployee(id);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateEmployee(@PathVariable Long id) throws APIException {
        return employeeService.activateEmployee(id);
    }

    @GetMapping("/{id}/leaves")
    public ResponseEntity<?> getEmployeeLeaves(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) throws APIException {
        return employeeService.getEmployeeLeaves(id, from, to);
    }

    @PostMapping("/{id}/leaves")
    public ResponseEntity<?> markEmployeeLeave(
            @PathVariable Long id,
            @RequestBody @Valid EmployeeLeaveMarkReq req
    ) throws APIException {
        return employeeService.markEmployeeLeave(id, req);
    }

    @DeleteMapping("/{id}/leaves")
    public ResponseEntity<?> deleteEmployeeLeave(
            @PathVariable Long id,
            @RequestParam LocalDate leaveDate
    ) throws APIException {
        return employeeService.deleteEmployeeLeave(id, leaveDate);
    }
}