package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.InventoryUpdateReq;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    public ResponseEntity<ResponseObject> getAllInventory(){
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get All Inventory Successfully")
                        .data(productMapper.toProductInventoryRes(productRepository.findAllWithVariantsAndInventories()))
                        .build()
        );

    }

    @Transactional
    public List<ProductInventoryRes> listInventoryRows() {
        return productMapper.toProductInventoryRes(productRepository.findAllWithVariantsAndInventories());
    }

    @Transactional
    public ResponseEntity<ResponseObject> checkInventory(OrderCreateReq orderCreateReq) throws APIException {
        List<String> list = new ArrayList<>();
        for (OrderDetailCreateReq orderDetailCreateReq : orderCreateReq.getOrderDetailCreateReqs()){

            if(orderDetailCreateReq.getProductVariantId()!=null){
                Product product = productRepository.findById(orderDetailCreateReq.getProductVariantId()).orElseThrow(()-> new APIException(ErrorCode.PRODUCT_NOT_FOUND));
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
    public ResponseEntity<ResponseObject> deleteInventory(Long id) throws APIException {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(
                ()-> new APIException(ErrorCode.INVENTORY_NOT_FOUND));
        inventory.setActive(false);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(SuccessCode.REQUEST.getStatus(), "Delete Inventory Successfully",inventoryRepository.save(inventory))
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> updateInventory(Long id, InventoryUpdateReq req) throws APIException {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(
                ()-> new APIException(ErrorCode.INVENTORY_NOT_FOUND));

        inventory.setRemainingQty(req.getRemainingQty());
        if (inventoryRepository.existsByInventoryCode(req.getInventoryCode())){
            throw new APIException(ErrorCode.CONFLICT);
        }
        inventory.setInventoryCode(req.getInventoryCode());
        inventory.setCostPrice(req.getCostPrice());
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(SuccessCode.REQUEST.getStatus(), "Update Inventory Successfully",inventoryRepository.save(inventory))
        );
    }

//    @Transactional
//    public ResponseEntity<ResponseObject> checkLotCode(String lotCode) throws APIException {
//        List<Inventory> inventory = inventoryRepository.findBylotCode(id).orElseThrow(
//                ()-> APIException.builder()
//                        .status(ErrorCode.NOT_FOUND.getStatus())
//                        .message("Cannot Found Inventory With Id = "+ id)
//                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
//                        .build());
//
//        inventory.setRemainingQty(req.getRemainingQty());
//        inventory.setLotCode(req.getLotCode());
//        inventory.setCostPrice(req.getCostPrice());
//        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
//                new ResponseObject(SuccessCode.REQUEST.getStatus(), "Update Inventory Successfully",inventoryRepository.save(inventory))
//        );
//    }


}
