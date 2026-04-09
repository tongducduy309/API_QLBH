package com.gener.qlbh.dtos.response;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.gener.qlbh.models.Category;
import com.gener.qlbh.models.Product;
import com.gener.qlbh.models.ProductVariant;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class InventoryRes {
    private Long id;
    private Double totalQty;
    private Double cost;
    private Long productId;
    private String name;
    private boolean wishlist;
    private String baseUnit;
    private Category category;
    private String variantCode;
    private String weight;
    private Double retailPrice;
    private Double storePrice;
    private Long variantId;
    private boolean status;
}
