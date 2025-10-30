package com.gener.qlbh.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "invoice_sequence")
@Getter
@Setter
public class InvoiceSequence {
    @Id
    private String period;
    @Column(name = "next_val", nullable = false)
    private Long nextVal;
}
