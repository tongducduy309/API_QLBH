package com.gener.qlbh.mapper;



import com.gener.qlbh.dtos.response.InventoryRes;
import com.gener.qlbh.models.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    List<InventoryRes> toInventoryRes(List<Inventory> reqs);

    @Mapping(target = "productId",source = "variant.product.id")
    @Mapping(target = "name",source = "variant.product.name")
//    @Mapping(target = "wishlist",source = "variant.product.wishlist")
    @Mapping(target = "baseUnit",source = "variant.product.baseUnit")
//    @Mapping(target = "category",source = "variant.product.category")
    @Mapping(target = "variantCode",source = "variant.variantCode")
    @Mapping(target = "weight",source = "variant.weight")
    @Mapping(target = "retailPrice",source = "variant.retailPrice")
    @Mapping(target = "storePrice",source = "variant.storePrice")
    @Mapping(target = "variantId",source = "variant.id")
    @Mapping(target = "status",source = "variant.businessStatus")
    InventoryRes toInventoryRes(Inventory req);
}
