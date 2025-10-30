package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.PurchaseOrderCreateReq;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.Method;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.mapper.PurchaseOrderMapper;
import com.gener.qlbh.models.*;
import com.gener.qlbh.repositories.InventoryRepository;
import com.gener.qlbh.repositories.ProductRepository;
import com.gener.qlbh.repositories.PurchaseOrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final InventoryRepository inventoryRepository;

    public ResponseEntity<ResponseObject> getAllPurchaseOrder(){
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get All Purchase Order Successfully")
                        .data(purchaseOrderRepository.findAll())
                        .build()
        );

    }

    @Transactional
    public ResponseEntity<ResponseObject> createPurchaseOrder(PurchaseOrderCreateReq req) throws APIException {
        Product product = productRepository.findById(req.getProductId()).orElseThrow(()-> APIException.builder()
                .status(ErrorCode.NOT_FOUND.getStatus())
                .message("Cannot Found Purchase Order With Id = "+req.getProductId())
                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                .build());

        Double length =req.getTotalLength();
        Double qty = req.getStockingQty();

        PurchaseOrder purchaseOrder = purchaseOrderMapper.toPurchaseOrder(req);
        purchaseOrder.setProduct(product);

        if (product.getCategory().getMethod().equals(Method.SHEET_METAL) && req.getStockingQty()==null){
            purchaseOrder.setStockingQty(1d);
            qty=1d;
        }
        if (product.getCategory().getMethod().equals(Method.MISC)){
            purchaseOrder.setTotalLength(1.0);
            length=1.0;
        }
        Double totalBaseUnitQty = qty*length;

        Inventory inventory = product.getInventory();
        inventory.setTotalBaseUnitQty(inventory.getTotalBaseUnitQty()+totalBaseUnitQty);
        product.setCostPrice(req.getCostPerUnit());
        purchaseOrder.setProduct(product);
        inventoryRepository.save(inventory);

        return ResponseEntity.status(SuccessCode.CREATE.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.CREATE.getStatus())
                        .message("Create Purchase Order Successfully")
                        .data(purchaseOrderRepository.save(purchaseOrder))
                        .build()
        );

    }
}
