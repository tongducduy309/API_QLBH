package com.gener.qlbh.mapper;

import com.gener.qlbh.dtos.request.ProductCreateReq;
import com.gener.qlbh.dtos.request.ProductUpdateReq;
import com.gener.qlbh.dtos.request.ProductVariantCreateReq;
import com.gener.qlbh.dtos.request.ProductVariantUpdateReq;
import com.gener.qlbh.dtos.response.ProductInventoryRes;
import com.gener.qlbh.dtos.response.ProductVariantInventoryRes;
import com.gener.qlbh.dtos.response.ProductVariantRes;
import com.gener.qlbh.models.Inventory;
import com.gener.qlbh.models.Product;
import com.gener.qlbh.models.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "description", source = "description")
    Product toProduct(ProductCreateReq req);

    Product toProduct(ProductUpdateReq req);

    ProductVariant toProductVariant(ProductVariantCreateReq req);

    @Mapping(target = "variantCode", source = "variantCode")
    ProductVariant toProductVariant(ProductVariantUpdateReq req);

    @Mapping(target = "productName", source = "product.name")
    ProductVariantRes toProductVariantRes(ProductVariant productVariant);
    List<ProductVariantRes> toProductVariantRes(List<ProductVariant> productVariant);

    List<ProductVariant> toProductVariantList_Create(List<ProductVariantCreateReq> req);

    List<ProductVariant> toProductVariantList(List<ProductVariantUpdateReq> req);

    ProductInventoryRes toProductInventoryRes(Product product);

    List<ProductInventoryRes> toProductInventoryRes(List<Product> product);

    default List<ProductVariantInventoryRes> toProductVariantInventoryRes(ProductVariant productVariant) {
        if (productVariant == null) {
            return Collections.emptyList();
        }

        List<Inventory> inventories = productVariant.getInventories().stream()
                .filter(inventory -> inventory.getActive()==true)
                .toList();

        if (inventories.isEmpty()) {
            return List.of(mapVariantWithoutInventory(productVariant));
        }

        return inventories.stream()
                .filter(Objects::nonNull)

                .map(inventory -> mapVariantInventory(productVariant, inventory))

                .toList();
    }

    default List<ProductVariantInventoryRes> toProductVariantInventoryRes(List<ProductVariant> productVariants) {
        if (productVariants == null) {
            return Collections.emptyList();
        }

        return productVariants.stream()
                .filter(Objects::nonNull)
                .flatMap(variant -> toProductVariantInventoryRes(variant).stream())
                .toList();
    }

    default ProductVariantInventoryRes mapVariantInventory(ProductVariant variant, Inventory inventory) {
        if (variant == null || inventory == null) {
            return null;
        }

//        if (!inventory.getActive()){
//            return mapVariantWithoutInventory(variant);
//        }

        ProductVariantInventoryRes res = new ProductVariantInventoryRes();
        res.setInventoryId(inventory.getId());
        res.setSku(variant.getSku());
        res.setProductName(variant.getProduct().getName());
        res.setInventoryCode(inventory.getInventoryCode());
        res.setOriginalQty(inventory.getOriginalQty());
        res.setOutOfStock(inventory.isOutOfStock());
        res.setVariantId(variant.getId());
        res.setVariantCode(variant.getVariantCode());
        res.setWeight(variant.getWeight());
        res.setRetailPrice(variant.getRetailPrice());
        res.setStorePrice(variant.getStorePrice());
        res.setRemainingQty(inventory.getRemainingQty());
        res.setCostPrice(inventory.getCostPrice());
        res.setActive(variant.getBusinessStatus());
        return res;
    }

    default ProductVariantInventoryRes mapVariantWithoutInventory(ProductVariant variant) {
        ProductVariantInventoryRes res = new ProductVariantInventoryRes();
        res.setInventoryId(null);
        res.setSku(variant.getSku());
        res.setInventoryCode(null);
        res.setOriginalQty(0.0);
        res.setOutOfStock(true);
        res.setVariantId(variant.getId());
        res.setVariantCode(variant.getVariantCode());
        res.setWeight(variant.getWeight());
        res.setRetailPrice(variant.getRetailPrice());
        res.setStorePrice(variant.getStorePrice());
        res.setRemainingQty(0.0);
        res.setCostPrice(0.0);
        res.setActive(variant.isActive());

        return res;
    }
}