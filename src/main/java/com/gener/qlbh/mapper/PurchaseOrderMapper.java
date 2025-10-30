package com.gener.qlbh.mapper;


import com.gener.qlbh.dtos.request.PurchaseOrderCreateReq;
import com.gener.qlbh.models.PurchaseOrder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PurchaseOrderMapper {
    PurchaseOrder toPurchaseOrder(PurchaseOrderCreateReq req);
}
