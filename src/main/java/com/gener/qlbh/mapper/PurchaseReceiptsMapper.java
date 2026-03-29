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
    PurchaseReceipts toPurchaseReceipts(PurchaseReceiptsCreateReq req);


    @Mapping(target = "name",source = "name")
    PurchaseReceiptsRes toPurchaseReceiptsRes(PurchaseReceipts req);

    List<PurchaseReceiptsRes> toPurchaseReceiptsRes(List<PurchaseReceipts> reqs);
}
