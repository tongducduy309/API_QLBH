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
import com.gener.qlbh.repositories.ProductVariantRepository;
import com.gener.qlbh.repositories.PurchaseOrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final InventoryRepository inventoryRepository;
    private final ProductVariantRepository productVariantRepository;

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
        ProductVariant productVariant = productVariantRepository.findById(req.getProductVariantId()).orElseThrow(()-> APIException.builder()
                .status(ErrorCode.NOT_FOUND.getStatus())
                .message("Cannot Found Product Variant With Id = "+req.getProductVariantId())
                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                .build());

        PurchaseOrder purchaseOrder = purchaseOrderMapper.toPurchaseOrder(req);
        purchaseOrder.setVariant(productVariant);

        Inventory inventory = new Inventory();

        if (productVariant.getProduct().getCategory().getMethod().equals(Method.SHEET_METAL)){
            inventory.setTotalQty(req.getTotalQty());

        }
        else{
            inventory = productVariant.getInventories().getFirst();
            inventory.addQuantity(req.getTotalQty());
        }
        inventory.setVariant(productVariant);
        productVariant.setCostPrice(req.getCostPerUnit());
        purchaseOrder.setVariant(productVariant);
        inventoryRepository.save(inventory);

        return ResponseEntity.status(SuccessCode.CREATE.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.CREATE.getStatus())
                        .message("Create Purchase Order Successfully")
                        .data(purchaseOrderRepository.save(purchaseOrder))
                        .build()
        );

    }

    @Transactional
    public ResponseEntity<ResponseObject> deletePurchaseOrder(Long id){
//        Optional<PurchaseOrder> purchaseOrder = purchaseOrderRepository.findById(id);
//        if (purchaseOrder.isPresent()){
//            Product product = purchaseOrder.get().getProduct();
//            Double length = purchaseOrder.get().getTotalLength();
//            Double qty = purchaseOrder.get().getStockingQty();
//            if (product.getCategory().getMethod().equals(Method.SHEET_METAL) && qty==null){
//                qty=1d;
//            }
//            if (product.getCategory().getMethod().equals(Method.MISC)){
//                length=1.0;
//            }
//            Double totalBaseUnitQty = qty*length;
//
////            Inventory inventory = product.getInventory();
////            inventory.exportInventory(totalBaseUnitQty);
////            inventoryRepository.save(inventory);
//            purchaseOrderRepository.deleteById(id);
//            Optional<PurchaseOrder> topPurchaseOrder = purchaseOrderRepository.findTopByProductIdOrderByCreatedAtDesc(product.getId());
//            if (topPurchaseOrder.isPresent()){
//                product.setCostPrice(topPurchaseOrder.get().getCostPerUnit());
//            }
//            else{
//                product.setCostPrice(0d);
//            }
//
//        }
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(SuccessCode.REQUEST.getStatus(), "Delete Purchase Order Successfully","")
        );
    }
}
