package com.gener.qlbh.utils;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public enum InventoryExcelColumn {
    PRODUCT_ID("productId", "ID sản phẩm", true),
    VARIANT_ID("variantId", "ID biến thể", true),
    INVENTORY_ID("inventoryId", "ID tồn kho", true),

    PRODUCT_NAME("productName", "Tên sản phẩm", false),
    CATEGORY_NAME("categoryName", "Danh mục", false),
    BASE_UNIT("baseUnit", "Đơn vị cơ bản", false),
    PRODUCT_ACTIVE("productActive", "Sản phẩm đang bán", false),
    PRODUCT_DESCRIPTION("description", "Mô tả", false),

    SKU("sku", "SKU", false),
    VARIANT_CODE("variantCode", "Mã biến thể", false),
    WEIGHT("weight", "Trọng lượng", false),
    RETAIL_PRICE("retailPrice", "Giá bán lẻ", false),
    STORE_PRICE("storePrice", "Giá bán buôn", false),
    VARIANT_ACTIVE("variantActive", "Biến thể đang bán", false),

    INVENTORY_CODE("inventoryCode", "Mã kho", false),
    ORIGINAL_QTY("originalQty", "SL ban đầu", false),
    REMAINING_QTY("remainingQty", "SL tồn", false),
    COST_PRICE("costPrice", "Giá vốn", false),
    OUT_OF_STOCK("outOfStock", "Hết hàng", false);

    private final String key;
    private final String header;
    private final boolean technical;

    InventoryExcelColumn(String key, String header, boolean technical) {
        this.key = key;
        this.header = header;
        this.technical = technical;
    }

    public static List<InventoryExcelColumn> exportOrder(List<String> selected) {
        LinkedHashSet<InventoryExcelColumn> ordered = new LinkedHashSet<>();

        ordered.add(PRODUCT_ID);
        ordered.add(VARIANT_ID);
        ordered.add(INVENTORY_ID);

        if (selected != null) {
            for (String key : selected) {
                fromKey(key).ifPresent(ordered::add);
            }
        }

        if (!ordered.contains(PRODUCT_NAME)) ordered.add(PRODUCT_NAME);
        if (!ordered.contains(SKU)) ordered.add(SKU);
        if (!ordered.contains(VARIANT_CODE)) ordered.add(VARIANT_CODE);
        if (!ordered.contains(INVENTORY_CODE)) ordered.add(INVENTORY_CODE);

        return new ArrayList<>(ordered);
    }

    public static Optional<InventoryExcelColumn> fromKey(String key) {
        if (key == null || key.isBlank()) return Optional.empty();
        return Arrays.stream(values())
                .filter(c -> c.key.equalsIgnoreCase(key.trim()))
                .findFirst();
    }

    public static List<String> visibleKeys() {
        return Arrays.stream(values())
                .filter(c -> !c.technical)
                .map(InventoryExcelColumn::getKey)
                .collect(Collectors.toList());
    }
}