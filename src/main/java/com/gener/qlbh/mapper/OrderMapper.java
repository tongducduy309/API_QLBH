package com.gener.qlbh.mapper;



import com.gener.qlbh.dtos.request.OrderDetailReq;
import com.gener.qlbh.dtos.request.OrderReq;
import com.gener.qlbh.dtos.response.OrderDetailRes;
import com.gener.qlbh.dtos.response.OrderRes;
import com.gener.qlbh.models.Customer;
import com.gener.qlbh.models.Order;
import com.gener.qlbh.models.OrderDetail;
import com.gener.qlbh.models.Product;
import com.gener.qlbh.repositories.CustomerRepository;
import com.gener.qlbh.repositories.ProductRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
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
    Order toOrder(OrderReq req);

    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "order",   ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "length",  source = "length")
    @Mapping(target = "quantity",source = "quantity")
    @Mapping(target = "price",source = "price")
    @Mapping(target = "name",source = "name")
    OrderDetail toOrderDetail(OrderDetailReq req);

    Set<OrderDetail> toEntityDetails(List<OrderDetailReq> reqs);


    OrderRes toOrderRes(Order order);


    @Mapping(target = "id",          source = "id")
    @Mapping(target = "length",      source = "length")
    @Mapping(target = "quantity",    source = "quantity")
    @Mapping(target = "price",        source = "price")
    @Mapping(target = "sku",         source = "product.sku")
    @Mapping(target = "name",        source = "name")
    @Mapping(target = "productId",   source = "product.id")
    @Mapping(target = "baseUnit",    source = "baseUnit")
    @Mapping(target = "lineTotal",    source = "subtotal")
    @Mapping(target = "totalLength",    source = "totalLength")
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
