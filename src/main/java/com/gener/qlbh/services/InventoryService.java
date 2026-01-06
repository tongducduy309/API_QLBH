package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.OrderDetailReq;
import com.gener.qlbh.dtos.request.OrderReq;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.mapper.OrderMapper;
import com.gener.qlbh.models.*;
import com.gener.qlbh.repositories.CategoryRepository;
import com.gener.qlbh.repositories.InventoryRepository;
import com.gener.qlbh.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    public ResponseEntity<ResponseObject> getAllInventory(){
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get All Inventory Successfully")
                        .data(inventoryRepository.findAll())
                        .build()
        );

    }

    @Transactional
    public ResponseEntity<ResponseObject> checkInventory(OrderReq orderReq) throws APIException {
        List<String> list = new ArrayList<>();
        for (OrderDetailReq orderDetailReq:orderReq.getOrderDetailReqs()){

            if(orderDetailReq.getProductId()!=null){
                Product product = productRepository.findById(orderDetailReq.getProductId()).orElseThrow(()-> APIException.builder()
                        .status(ErrorCode.NOT_FOUND.getStatus())
                        .message("Cannot Found Product With Id = "+orderDetailReq.getProductId())
                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                        .build());
                OrderDetail orderDetail = orderMapper.toOrderDetail(orderDetailReq);
                Double totalLength = orderDetail.getTotalLength();
//                Inventory inventory = product.getInventory();
//                if((totalLength!=null&&totalLength>inventory.getTotalBaseUnitQty())||(totalLength==null&&orderDetail.getQuantity()>inventory.getTotalBaseUnitQty())){
////                    throw APIException.builder()
////                            .status(ErrorCode.BAD_REQUEST.getStatus())
////                            .message("Trong kho không có đủ số lượng sản phẩm có id = "+product.getId())
////                            .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatusCode())
////                            .build();
//                    list.add(product.getName());
//                }
            }
        }
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Check Inventory Successfully")
                        .data(list)
                        .build()
        );
    }
}
