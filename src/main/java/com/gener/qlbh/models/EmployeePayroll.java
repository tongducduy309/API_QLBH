package com.gener.qlbh.models;
import com.gener.qlbh.enums.EmployeeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
@Entity
@Table(
        name = "employee_payrolls",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "month", "year"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class EmployeePayroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "payroll_month", nullable = false)
    private Integer month;

    @Column(name = "payroll_year", nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Double baseSalary;

    @Column(nullable = false)

    private Double leaveDeduction;

    @Column(nullable = false)

    private Double allowance;

    @Column(nullable = false)

    private Double bonus;

    @Column(nullable = false)

    private Double penalty;

    @Column(nullable = false)
    private Double netSalary;
}
