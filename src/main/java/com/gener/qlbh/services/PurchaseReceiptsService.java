package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.PurchaseReceiptsCreateReq;
import com.gener.qlbh.dtos.response.PurchaseReceiptDetailRes;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.PurchaseReceiptMethod;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.mapper.PurchaseReceiptsMapper;
import com.gener.qlbh.models.Inventory;
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
    private final InventoryCodeGeneratorService inventoryCodeGeneratorService;

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

    @Transactional(rollbackOn  = Exception.class)
    public ResponseEntity<ResponseObject> createPurchaseReceipts(PurchaseReceiptsCreateReq req) throws APIException {
        validateCreateRequest(req);

        ProductVariant productVariant = productVariantRepository.findById(req.getProductVariantId())
                .orElseThrow(() -> new APIException(ErrorCode.VARIANT_NOT_FOUND));

        PurchaseReceiptMethod method = Optional.ofNullable(req.getPurchaseReceiptMethod())
                .orElse(PurchaseReceiptMethod.SEPARATE);

        PurchaseReceipts purchaseReceipts = purchaseReceiptsMapper.toPurchaseReceipts(req);
        purchaseReceipts.setVariant(productVariant);
        purchaseReceipts.setPurchaseReceiptMethod(method);

        Inventory inventory = buildInventory(req, productVariant, method);

        inventory = inventoryRepository.save(inventory);

        purchaseReceipts.setInventory(inventory);

        PurchaseReceipts savedReceipt = purchaseReceiptsRepository.save(purchaseReceipts);

        return ResponseEntity.status(SuccessCode.CREATE.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.CREATE.getStatus())
                        .message("Create Purchase Receipts Successfully")
                        .data(savedReceipt)
                        .build()
        );
    }

    @Transactional(rollbackOn  = Exception.class)
    public ResponseEntity<ResponseObject> deletePurchaseReceipts(Long id) throws APIException {
        PurchaseReceipts receipt = purchaseReceiptsRepository.findById(id)
                .orElseThrow(() -> new APIException(ErrorCode.PURCHASE_RECEIPT_NOT_FOUND));

        if (receipt.getTotalQuantity() == null || receipt.getTotalQuantity() <= 0) {
            throw new APIException(ErrorCode.PURCHASE_RECEIPT_QUANTITY_INVALID);
        }

        if (receipt.getInventory() == null || receipt.getInventory().getId() == null) {
            throw new APIException(ErrorCode.PURCHASE_RECEIPT_INVENTORY_NOT_FOUND);
        }

        Inventory inventory = inventoryRepository.findById(receipt.getInventory().getId())
                .orElseThrow(() -> new APIException(ErrorCode.INVENTORY_NOT_FOUND));

        Double qty = receipt.getTotalQuantity();
        Double currentOriginalQty = inventory.getOriginalQty() == null ? 0D : inventory.getOriginalQty();
        Double currentRemainingQty = inventory.getRemainingQty() == null ? 0D : inventory.getRemainingQty();

        if (currentRemainingQty < qty) {
            throw new APIException(ErrorCode.PURCHASE_RECEIPT_DELETE_STOCK_NOT_ENOUGH);
        }

        double newOriginalQty = currentOriginalQty - qty;
        double newRemainingQty = currentRemainingQty - qty;

        purchaseReceiptsRepository.delete(receipt);

        if (newOriginalQty <= 0 && newRemainingQty <= 0) {
            inventoryRepository.delete(inventory);
        } else {
            inventory.setOriginalQty(Math.max(newOriginalQty, 0D));
            inventory.setRemainingQty(Math.max(newRemainingQty, 0D));
            inventoryRepository.save(inventory);
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
                .orElseThrow(() -> new APIException(ErrorCode.PURCHASE_RECEIPT_NOT_FOUND));

        ProductVariant variant = receipt.getVariant();
        Product product = variant != null ? variant.getProduct() : null;
        Inventory inventory = receipt.getInventory();

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

                .inventoryId(inventory != null ? inventory.getId() : null)
                .inventoryCode(inventory != null ? inventory.getInventoryCode() : null)
                .inventoryOriginalQty(inventory != null ? inventory.getOriginalQty() : null)
                .inventoryRemainingQty(inventory != null ? inventory.getRemainingQty() : null)
                .inventoryCostPrice(inventory != null ? inventory.getCostPrice() : null)
                .inventoryImportedAt(inventory != null ? inventory.getImportedAt() : null)
                .inventoryActive(inventory != null ? inventory.getActive() : null)
                .build();

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get Purchase Receipt Detail Successfully")
                        .data(data)
                        .build()
        );
    }

    private void validateCreateRequest(PurchaseReceiptsCreateReq req) throws APIException {
        if (req.getProductVariantId() == null) {
            throw new APIException(ErrorCode.PRODUCT_VARIANT_ID_REQUIRED);
        }

        if (req.getTotalQuantity() == null || req.getTotalQuantity() <= 0) {
            throw new APIException(ErrorCode.PURCHASE_RECEIPT_TOTAL_QUANTITY_INVALID);
        }

        if (req.getCost() == null || req.getCost() < 0) {
            throw new APIException(ErrorCode.PURCHASE_RECEIPT_COST_INVALID);
        }
    }

    private Inventory buildInventory(
            PurchaseReceiptsCreateReq req,
            ProductVariant productVariant,
            PurchaseReceiptMethod method
    ) throws APIException {
        Inventory inventory;

        if (method == PurchaseReceiptMethod.ADDITIVE) {
            inventory = inventoryRepository.findByVariantId(req.getProductVariantId());

            if (inventory == null) {
                inventory = Inventory.builder()
                        .variant(productVariant)
                        .originalQty(req.getTotalQuantity())
                        .remainingQty(req.getTotalQuantity())
                        .costPrice(req.getCost())
                        .importedAt(LocalDateTime.now())
                        .build();
            } else {
                Double currentOriginalQty = inventory.getOriginalQty() == null ? 0D : inventory.getOriginalQty();
                Double currentRemainingQty = inventory.getRemainingQty() == null ? 0D : inventory.getRemainingQty();

                inventory.setOriginalQty(currentOriginalQty + req.getTotalQuantity());
                inventory.setRemainingQty(currentRemainingQty + req.getTotalQuantity());
                inventory.setCostPrice(req.getCost());

                if (inventory.getImportedAt() == null) {
                    inventory.setImportedAt(LocalDateTime.now());
                }
            }
        } else {
            inventory = Inventory.builder()
                    .variant(productVariant)
                    .originalQty(req.getTotalQuantity())
                    .remainingQty(req.getTotalQuantity())
                    .costPrice(req.getCost())
                    .importedAt(LocalDateTime.now())
                    .build();
        }

        applyInventoryCode(req, inventory);
        return inventory;
    }

    private void applyInventoryCode(PurchaseReceiptsCreateReq req, Inventory inventory) throws APIException {
        String requestInventoryCode = req.getInventoryCode();

        if (requestInventoryCode == null || requestInventoryCode.isBlank()) {
            if (inventory.getInventoryCode() == null || inventory.getInventoryCode().isBlank()) {
                inventory.setInventoryCode(inventoryCodeGeneratorService.generateNextCode());
            }
            return;
        }

        if (inventory.getId() != null && requestInventoryCode.equals(inventory.getInventoryCode())) {
            inventory.setInventoryCode(requestInventoryCode);
            return;
        }

        if (inventoryRepository.existsByInventoryCode(requestInventoryCode)) {
            throw new APIException(ErrorCode.INVENTORY_CODE_ALREADY_EXISTS);
        }

        inventory.setInventoryCode(requestInventoryCode);
    }
}