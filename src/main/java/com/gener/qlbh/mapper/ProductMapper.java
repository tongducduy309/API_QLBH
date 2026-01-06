package com.gener.qlbh.mapper;



import com.gener.qlbh.dtos.request.ProductCreateReq;
import com.gener.qlbh.dtos.request.ProductUpdateReq;
import com.gener.qlbh.dtos.request.ProductVariantCreateReq;
import com.gener.qlbh.dtos.request.ProductVariantUpdateReq;
import com.gener.qlbh.models.Product;
import com.gener.qlbh.models.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toProduct(ProductCreateReq req);
    Product toProduct(ProductUpdateReq req);
    ProductVariant toProductVariant(ProductVariantCreateReq req);
    @Mapping(target = "variantCode",source = "variantCode")
    ProductVariant toProductVariant(ProductVariantUpdateReq req);

    List<ProductVariant> toProductVariantList_Create(List<ProductVariantCreateReq> req);
    List<ProductVariant> toProductVariantList(List<ProductVariantUpdateReq> req);
}
