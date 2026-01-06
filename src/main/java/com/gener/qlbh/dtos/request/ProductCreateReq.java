package com.gener.qlbh.dtos.request;

import com.gener.qlbh.models.Category;
import com.gener.qlbh.models.Inventory;
import com.gener.qlbh.models.ProductVariant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductCreateReq {
    @NotNull
    private String name;
    @NotNull
    private boolean status;
    private String baseUnit;
    @NotNull
    private Long categoryId;

    private List<ProductVariantCreateReq> variants;
}
