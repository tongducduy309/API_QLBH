package com.gener.qlbh.mapper;


import com.gener.qlbh.dtos.request.PurchaseReceiptsCreateReq;
import com.gener.qlbh.dtos.response.PurchaseReceiptsRes;
import com.gener.qlbh.models.PurchaseReceipts;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PurchaseReceiptsMapper {
    @Mapping(target = "totalQuantity",source = "totalQuantity")
    @Mapping(target = "cost",source = "cost")
    @Mapping(target = "totalCost",source = "totalCost")
    PurchaseReceipts toPurchaseReceipts(PurchaseReceiptsCreateReq req);


    @Mapping(target = "name",source = "name")
    @Mapping(target = "productVariantCode",source = "variant.variantCode")
    @Mapping(target = "productVariantSKU",source = "variant.sku")
    @Mapping(target = "purchaseReceiptMethod",source = "purchaseReceiptMethod")
    @Mapping(target = "productId",source = "variant.product.id")
    PurchaseReceiptsRes toPurchaseReceiptsRes(PurchaseReceipts req);

    List<PurchaseReceiptsRes> toPurchaseReceiptsRes(List<PurchaseReceipts> reqs);
}
