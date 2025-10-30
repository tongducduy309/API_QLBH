package com.gener.qlbh.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "order_sequence")
public class OrderSequence {
    @Id
    @Column(length = 6)            // ví dụ: "102025"
    private String period;

    @Column(name = "next_val", nullable = false)
    private Long nextVal;
}
