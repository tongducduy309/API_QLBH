package com.gener.qlbh.dtos.request;

import com.gener.qlbh.models.Category;
import com.gener.qlbh.models.Inventory;
import com.gener.qlbh.models.StockDetail;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductCreateReq {
    private String sku;
    @NotNull
    private String name;
    private Double retailPrice;
    private Double storePrice;
    @NotNull
    private boolean status;
    private String baseUnit;
    @NotNull
    private Long categoryId;
}
