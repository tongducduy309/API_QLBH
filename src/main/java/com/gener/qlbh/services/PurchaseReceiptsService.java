package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.PurchaseReceiptsCreateReq;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.PurchaseReceiptMethod;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.mapper.PurchaseReceiptsMapper;
import com.gener.qlbh.models.*;
import com.gener.qlbh.repositories.InventoryRepository;
import com.gener.qlbh.repositories.ProductRepository;
import com.gener.qlbh.repositories.ProductVariantRepository;
import com.gener.qlbh.repositories.PurchaseReceiptsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PurchaseReceiptsService {
    private final PurchaseReceiptsRepository purchaseReceiptsRepository;
    private final ProductRepository productRepository;
    private final PurchaseReceiptsMapper purchaseReceiptsMapper;
    private final InventoryRepository inventoryRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public ResponseEntity<ResponseObject> getAllPurchaseReceipts(){
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get All Purchase Receipts Successfully")
                        .data(purchaseReceiptsMapper.toPurchaseReceiptsRes(purchaseReceiptsRepository.findAll()))
                        .build()
        );

    }

    @Transactional
    public ResponseEntity<ResponseObject> getAllPurchaseReceiptsByProductId(Long productId){
        List<PurchaseReceipts> purchaseReceipts = purchaseReceiptsRepository.findAllByProductId(productId);
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get All Purchase Receipts By Product ID Successfully")
                        .data(purchaseReceiptsMapper.toPurchaseReceiptsRes(purchaseReceipts))
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> createPurchaseReceipts(PurchaseReceiptsCreateReq req) throws APIException {
        ProductVariant productVariant = productVariantRepository.findById(req.getProductVariantId()).orElseThrow(()-> APIException.builder()
                .status(ErrorCode.NOT_FOUND.getStatus())
                .message("Cannot Found Product Variant With Id = "+req.getProductVariantId())
                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                .build());

        PurchaseReceiptMethod method = Optional
                .ofNullable(req.getPurchaseReceiptMethod())
                .orElse(PurchaseReceiptMethod.SEPARATE);


        PurchaseReceipts purchaseReceipts = purchaseReceiptsMapper.toPurchaseReceipts(req);
        purchaseReceipts.setPurchaseReceiptMethod(method);
        purchaseReceipts.setVariant(productVariant);

        InventoryLot inventoryLot = new InventoryLot();
        inventoryLot = inventoryRepository.findByVariantId(req.getProductVariantId());
        if (method.equals(PurchaseReceiptMethod.ADDITIVE)&& inventoryLot !=null){
//            inventoryLot.addQuantity(req.getTotalQuantity());
        }else{
            inventoryLot = InventoryLot.builder()
                    .variant(productVariant)
                    .originalQty(req.getTotalQuantity())
                    .remainingQty(req.getTotalQuantity())
//                    .totalQty(req.getTotalQuantity())
                    .build();
        }
        inventoryLot.setCostPrice(req.getCost());
        inventoryRepository.save(inventoryLot);

        return ResponseEntity.status(SuccessCode.CREATE.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.CREATE.getStatus())
                        .message("Create Purchase Receipts Successfully")
                        .data(purchaseReceiptsRepository.save(purchaseReceipts))
                        .build()
        );

    }

    @Transactional
    public ResponseEntity<ResponseObject> deletePurchaseReceipts(Long id) throws APIException {

        PurchaseReceipts receipt = purchaseReceiptsRepository.findById(id)
                .orElseThrow(() -> APIException.builder()
                        .status(ErrorCode.NOT_FOUND.getStatus())
                        .message("Cannot Found PurchaseReceipt With Id = " + id)
                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                        .build());

//        ProductVariant variant = receipt.getVariant();
//        if (variant == null) {
//            throw APIException.builder()
//                    .status(ErrorCode.BAD_REQUEST.getStatus())
//                    .message("PurchaseReceipt has no product variant")
//                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatusCode())
//                    .build();
//        }

        Double qty = receipt.getTotalQuantity();
        if (qty == null || qty <= 0) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getStatus())
                    .message("Invalid receipt quantity")
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatusCode())
                    .build();
        }

        InventoryLot inv = inventoryRepository.findById(receipt.getInventoryId())
                .orElseThrow(() -> APIException.builder()
                        .status(ErrorCode.NOT_FOUND.getStatus())
                        .message("Cannot Found Inventory With Id = " + receipt.getInventoryId())
                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                        .build());

        // Check không cho âm kho
        if (inv.getRemainingQty() < qty) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getStatus())
                    .message("Cannot delete receipt because current stock is smaller than receipt quantity")
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatusCode())
                    .build();
        }

//        inv.subQuantity(qty);
//        inv.setOriginalQty(qty);
        inventoryRepository.save(inv);

        purchaseReceiptsRepository.delete(receipt);



        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(SuccessCode.REQUEST.getStatus(),
                        "Delete Purchase Receipts Successfully", "")
        );
    }
}
