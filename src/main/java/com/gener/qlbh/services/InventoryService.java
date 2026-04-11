package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.OrderDetailCreateReq;
import com.gener.qlbh.dtos.request.OrderCreateReq;
import com.gener.qlbh.dtos.response.ProductInventoryRes;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.mapper.InventoryMapper;
import com.gener.qlbh.mapper.OrderMapper;
import com.gener.qlbh.mapper.ProductMapper;
import com.gener.qlbh.models.*;
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
    private final InventoryMapper inventoryMapper;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    public ResponseEntity<ResponseObject> getAllInventory(boolean status){
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get All Inventory Successfully")
                        .data(productMapper.toProductInventoryRes(productRepository.findAllWithVariantsAndInventories(status)))
                        .build()
        );

    }

    @Transactional
    public List<ProductInventoryRes> listInventoryRows(boolean status) {
        return productMapper.toProductInventoryRes(productRepository.findAllWithVariantsAndInventories(status));
    }

    @Transactional
    public ResponseEntity<ResponseObject> checkInventory(OrderCreateReq orderCreateReq) throws APIException {
        List<String> list = new ArrayList<>();
        for (OrderDetailCreateReq orderDetailCreateReq : orderCreateReq.getOrderDetailCreateReqs()){

            if(orderDetailCreateReq.getProductVariantId()!=null){
                Product product = productRepository.findById(orderDetailCreateReq.getProductVariantId()).orElseThrow(()-> APIException.builder()
                        .status(ErrorCode.NOT_FOUND.getStatus())
                        .message("Cannot Found Product With Id = "+ orderDetailCreateReq.getProductVariantId())
                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                        .build());
                OrderDetail orderDetail = orderMapper.toOrderDetail(orderDetailCreateReq);
                Double totalLength = orderDetail.getTotalQuantity();
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

    @Transactional
    public ResponseEntity<ResponseObject> deleteInventory(Long id){
        Optional<InventoryLot> inventory = inventoryRepository.findById(id);
        if (inventory.isPresent()){

            inventoryRepository.deleteById(id);

        }
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(SuccessCode.REQUEST.getStatus(), "Delete Inventory Successfully","")
        );
    }
}
