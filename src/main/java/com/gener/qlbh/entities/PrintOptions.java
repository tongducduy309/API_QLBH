package com.gener.qlbh.entities;

import com.gener.qlbh.enums.PageOrientation;
import com.gener.qlbh.enums.PaperSize;
import jakarta.persistence.Entity;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PrintOptions {
    private PaperSize paperSize;
    private PageOrientation pageOrientation;
    private String deviceName;
    private Integer copies;
}
