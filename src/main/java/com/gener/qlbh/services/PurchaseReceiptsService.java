package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.PurchaseReceiptsCreateReq;
import com.gener.qlbh.dtos.response.PurchaseReceiptDetailRes;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.PurchaseReceiptMethod;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.mapper.PurchaseReceiptsMapper;
import com.gener.qlbh.models.InventoryLot;
import com.gener.qlbh.models.Product;
import com.gener.qlbh.models.ProductVariant;
import com.gener.qlbh.models.PurchaseReceipts;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.repositories.InventoryRepository;
import com.gener.qlbh.repositories.ProductVariantRepository;
import com.gener.qlbh.repositories.PurchaseReceiptsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PurchaseReceiptsService {

    private final PurchaseReceiptsRepository purchaseReceiptsRepository;
    private final PurchaseReceiptsMapper purchaseReceiptsMapper;
    private final InventoryRepository inventoryRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public ResponseEntity<ResponseObject> getAllPurchaseReceipts() {
        List<PurchaseReceipts> purchaseReceipts = purchaseReceiptsRepository.findAll();

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get All Purchase Receipts Successfully")
                        .data(purchaseReceiptsMapper.toPurchaseReceiptsRes(purchaseReceipts))
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> getAllPurchaseReceiptsByProductId(Long productId) {
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
        if (req.getProductVariantId() == null) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getStatus())
                    .message("ProductVariantId is required")
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatusCode())
                    .build();
        }

        if (req.getTotalQuantity() == null || req.getTotalQuantity() <= 0) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getStatus())
                    .message("Total quantity must be greater than 0")
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatusCode())
                    .build();
        }

        if (req.getCost() == null || req.getCost() < 0) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getStatus())
                    .message("Cost must be greater than or equal to 0")
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatusCode())
                    .build();
        }

        ProductVariant productVariant = productVariantRepository.findById(req.getProductVariantId())
                .orElseThrow(() -> APIException.builder()
                        .status(ErrorCode.NOT_FOUND.getStatus())
                        .message("Cannot Found Product Variant With Id = " + req.getProductVariantId())
                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                        .build());

        PurchaseReceiptMethod method = Optional.ofNullable(req.getPurchaseReceiptMethod())
                .orElse(PurchaseReceiptMethod.SEPARATE);

        PurchaseReceipts purchaseReceipts = purchaseReceiptsMapper.toPurchaseReceipts(req);
        purchaseReceipts.setVariant(productVariant);
        purchaseReceipts.setPurchaseReceiptMethod(method);

        InventoryLot inventoryLot;

        if (method == PurchaseReceiptMethod.ADDITIVE) {
            inventoryLot = inventoryRepository.findByVariantId(req.getProductVariantId());

            if (inventoryLot == null) {
                inventoryLot = InventoryLot.builder()
                        .variant(productVariant)
                        .originalQty(req.getTotalQuantity())
                        .remainingQty(req.getTotalQuantity())
                        .costPrice(req.getCost())
                        .importedAt(LocalDateTime.now())
                        .build();
            } else {
                Double currentOriginalQty = inventoryLot.getOriginalQty() == null ? 0D : inventoryLot.getOriginalQty();
                Double currentRemainingQty = inventoryLot.getRemainingQty() == null ? 0D : inventoryLot.getRemainingQty();

                inventoryLot.setOriginalQty(currentOriginalQty + req.getTotalQuantity());
                inventoryLot.setRemainingQty(currentRemainingQty + req.getTotalQuantity());
                inventoryLot.setCostPrice(req.getCost());
                inventoryLot.setLotCode(req.getLotCode());

                if (inventoryLot.getImportedAt() == null) {
                    inventoryLot.setImportedAt(LocalDateTime.now());
                }
            }
        } else {
            inventoryLot = InventoryLot.builder()
                    .variant(productVariant)
                    .originalQty(req.getTotalQuantity())
                    .remainingQty(req.getTotalQuantity())
                    .costPrice(req.getCost())
                    .importedAt(LocalDateTime.now())
                    .lotCode(req.getLotCode())
                    .build();
        }

        inventoryLot = inventoryRepository.save(inventoryLot);

        purchaseReceipts.setInventory(inventoryLot);

        PurchaseReceipts savedReceipt = purchaseReceiptsRepository.save(purchaseReceipts);

        return ResponseEntity.status(SuccessCode.CREATE.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.CREATE.getStatus())
                        .message("Create Purchase Receipts Successfully")
                        .data(savedReceipt)
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

        if (receipt.getTotalQuantity() == null || receipt.getTotalQuantity() <= 0) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getStatus())
                    .message("Invalid receipt quantity")
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatusCode())
                    .build();
        }

        if (receipt.getInventory() == null || receipt.getInventory().getId() == null) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getStatus())
                    .message("PurchaseReceipt has no inventory linked")
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatusCode())
                    .build();
        }

        InventoryLot inventoryLot = inventoryRepository.findById(receipt.getInventory().getId())
                .orElseThrow(() -> APIException.builder()
                        .status(ErrorCode.NOT_FOUND.getStatus())
                        .message("Cannot Found Inventory With Id = " + receipt.getInventory().getId())
                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                        .build());

        Double qty = receipt.getTotalQuantity();
        Double currentOriginalQty = inventoryLot.getOriginalQty() == null ? 0D : inventoryLot.getOriginalQty();
        Double currentRemainingQty = inventoryLot.getRemainingQty() == null ? 0D : inventoryLot.getRemainingQty();

        if (currentRemainingQty < qty) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getStatus())
                    .message("Cannot delete receipt because current stock is smaller than receipt quantity")
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatusCode())
                    .build();
        }

        double newOriginalQty = currentOriginalQty - qty;
        double newRemainingQty = currentRemainingQty - qty;

        purchaseReceiptsRepository.delete(receipt);

        if (newOriginalQty <= 0 && newRemainingQty <= 0) {
            inventoryRepository.delete(inventoryLot);
        } else {
            inventoryLot.setOriginalQty(Math.max(newOriginalQty, 0D));
            inventoryLot.setRemainingQty(Math.max(newRemainingQty, 0D));
            inventoryRepository.save(inventoryLot);
        }

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Delete Purchase Receipts Successfully")
                        .data("")
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> getPurchaseReceiptDetail(Long id) throws APIException {
        PurchaseReceipts receipt = purchaseReceiptsRepository.findById(id)
                .orElseThrow(() -> APIException.builder()
                        .status(ErrorCode.NOT_FOUND.getStatus())
                        .message("Cannot Found PurchaseReceipt With Id = " + id)
                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                        .build());

        ProductVariant variant = receipt.getVariant();
        Product product = variant != null ? variant.getProduct() : null;
        InventoryLot inventoryLot = receipt.getInventory();

        PurchaseReceiptDetailRes data = PurchaseReceiptDetailRes.builder()
                .id(receipt.getId())

                .productId(product != null ? product.getId() : null)
                .productName(product != null ? product.getName() : null)

                .productVariantId(variant != null ? variant.getId() : null)
                .productVariantCode(variant != null ? variant.getVariantCode() : null)
                .productVariantSKU(variant != null ? variant.getSku() : null)
                .productVariantWeight(variant != null ? variant.getWeight() : null)

                .purchaseReceiptMethod(receipt.getPurchaseReceiptMethod())
                .totalQuantity(receipt.getTotalQuantity())
                .cost(receipt.getCost())
                .totalCost(receipt.getTotalCost())
                .supplier(receipt.getSupplier())
                .note(receipt.getNote())
                .createdAt(receipt.getCreatedAt())

                .inventoryLotId(inventoryLot != null ? inventoryLot.getId() : null)
                .inventoryLotCode(inventoryLot != null ? inventoryLot.getLotCode() : null)
                .inventoryOriginalQty(inventoryLot != null ? inventoryLot.getOriginalQty() : null)
                .inventoryRemainingQty(inventoryLot != null ? inventoryLot.getRemainingQty() : null)
                .inventoryCostPrice(inventoryLot != null ? inventoryLot.getCostPrice() : null)
                .inventoryImportedAt(inventoryLot != null ? inventoryLot.getImportedAt() : null)
                .inventoryActive(inventoryLot != null ? inventoryLot.getActive() : null)
                .build();

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get Purchase Receipt Detail Successfully")
                        .data(data)
                        .build()
        );
    }


}