package com.gener.qlbh.mapper;



import com.gener.qlbh.dtos.request.OrderCreateReq;
import com.gener.qlbh.dtos.request.OrderDetailCreateReq;
import com.gener.qlbh.dtos.request.OrderDetailUpdateReq;
import com.gener.qlbh.dtos.response.OrderDetailRes;
import com.gener.qlbh.dtos.response.OrderRecentRes;
import com.gener.qlbh.dtos.response.OrderRes;
import com.gener.qlbh.models.Order;
import com.gener.qlbh.models.OrderDetail;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Mapper(
        componentModel = "spring"
)
public interface OrderMapper {

    @Mapping(target = "customer",       ignore = true)
    @Mapping(target = "note",           source = "note")
    @Mapping(target = "paidAmount",  source = "paidAmount")
    @Mapping(target = "remainingAmount",ignore = true)
    @Mapping(target = "shippingFee",    source = "shippingFee")
    @Mapping(target = "subtotal",       ignore = true)
    @Mapping(target = "details",        ignore = true)
    @Mapping(target = "createdAt",      ignore = true)
    @Mapping(target = "nameCustomer",    source = "nameCustomer")
    @Mapping(target = "phoneCustomer",    source = "phoneCustomer")
    @Mapping(target = "addressCustomer",    source = "addressCustomer")
    Order toOrder(OrderCreateReq req);



    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "order",   ignore = true)
    @Mapping(target = "productVariant", ignore = true)
    @Mapping(target = "length",  source = "length")
    @Mapping(target = "quantity",source = "quantity")
    @Mapping(target = "price",source = "price")
    @Mapping(target = "name",source = "name")
    @Mapping(target = "baseUnit",source = "baseUnit")
    @Mapping(target = "inventory",ignore = true)
    @Mapping(target = "lineIndex",source = "lineIndex")
    @Mapping(target = "kind",source = "kind")
    OrderDetail toOrderDetail(OrderDetailCreateReq req);

    @Mapping(target = "id",      source = "id")
    @Mapping(target = "order",   ignore = true)
    @Mapping(target = "productVariant", ignore = true)
    @Mapping(target = "length",  source = "length")
    @Mapping(target = "quantity",source = "quantity")
    @Mapping(target = "price",source = "price")
    @Mapping(target = "name",source = "name")
    @Mapping(target = "baseUnit",source = "baseUnit")
    @Mapping(target = "inventory", ignore = true)
    @Mapping(target = "kind",source = "kind")
    OrderDetail toOrderDetail(OrderDetailUpdateReq req);

    Set<OrderDetail> toEntityDetails_Create(List<OrderDetailCreateReq> reqs);

    Set<OrderDetail> toEntityDetails_Update(List<OrderDetailUpdateReq> reqs);

    @Mapping(target = "customer.id", source = "idCustomer")
    @Mapping(target = "customer.name", source = "nameCustomer")
    @Mapping(target = "customer.phone", source = "phoneCustomer")
    @Mapping(target = "customer.address", source = "addressCustomer")
    @Mapping(target = "taxAmount",source = "taxAmount")
    @Mapping(target = "total",source = "total")
    OrderRes toOrderRes(Order order);



    List<OrderRes> toOrderRes(List<Order> orders);

    @Mapping(target = "customerName", source = "nameCustomer")
    @Mapping(target = "total",source = "total")
    OrderRecentRes toOrderRecentRes(Order order);

    List<OrderRecentRes> toOrderRecentRes(List<Order> orders);

    @Mapping(target = "id",          source = "id")
    @Mapping(target = "length",      source = "length")
    @Mapping(target = "quantity",    source = "quantity")
    @Mapping(target = "price",        source = "price")
    @Mapping(target = "sku",         source = "productVariant.sku")
    @Mapping(target = "name",        source = "name")
    @Mapping(target = "productVariantId",   source = "productVariant.id")
    @Mapping(target = "baseUnit",    source = "baseUnit")
    @Mapping(target = "subtotal",    source = "subtotal")
    @Mapping(target = "totalQuantity",    source = "totalQuantity")
    @Mapping(target = "inventoryId",    source = "inventory.id")
    @Mapping(target = "kind",source = "kind")
    OrderDetailRes toOrderDetailRes(OrderDetail orderDetail);

    @Mapping(target = "total",    source = "total")
    Set<OrderDetailRes> toOrderDetailResSet(Set<OrderDetail> details);

    List<OrderDetailRes> toOrderDetailResList(List<OrderDetail> orderDetails);

    @AfterMapping
    default void ensureDetailsNotNull(@MappingTarget OrderRes target) {
        if (target.getDetails() == null) {
            target.setDetails(Collections.emptySet());
        }
    }

}
