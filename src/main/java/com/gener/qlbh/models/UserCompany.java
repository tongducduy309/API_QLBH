package com.gener.qlbh.models;

import com.gener.qlbh.enums.CompanyRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_companies",
        uniqueConstraints = @UniqueConstraint(columnNames={"user_id","company_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCompany {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id", nullable=false)
    private User user;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id", nullable=false)
    private Company company;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private CompanyRole role = CompanyRole.STAFF;
}
