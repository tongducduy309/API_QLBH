package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.InventoryExcelExportReq;
import com.gener.qlbh.dtos.response.InventoryExcelImportRes;
import com.gener.qlbh.dtos.response.ProductInventoryRes;
import com.gener.qlbh.dtos.response.ProductVariantInventoryRes;
import com.gener.qlbh.models.Inventory;
import com.gener.qlbh.models.Product;
import com.gener.qlbh.models.ProductVariant;
import com.gener.qlbh.repositories.CategoryRepository;
import com.gener.qlbh.repositories.InventoryRepository;
import com.gener.qlbh.repositories.ProductRepository;
import com.gener.qlbh.repositories.ProductVariantRepository;
import com.gener.qlbh.utils.InventoryExcelColumn;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InventoryExcelService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryRepository inventoryRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryService inventoryService;

    public byte[] exportInventoryExcel(InventoryExcelExportReq req) throws IOException {
        boolean onlyActive = req.getOnlyActive() == null || req.getOnlyActive();
        @SuppressWarnings("unchecked")
        List<ProductInventoryRes> items = (List<ProductInventoryRes>) inventoryService
                .getAllInventory()
                .getBody();

        // inventoryService.getAllInventory trả ResponseObject, nên ở controller sẽ truyền data trực tiếp tốt hơn.
        // Nếu bạn muốn gọn hơn thì đổi InventoryService thêm hàm listInventoryRows(boolean).
        throw new UnsupportedOperationException("Dùng phiên bản service ở mục 2.6 bên dưới để tránh parse ResponseObject");
    }

    public byte[] exportInventoryExcel(List<ProductInventoryRes> items, InventoryExcelExportReq req) throws IOException {
        List<InventoryExcelColumn> columns = InventoryExcelColumn.exportOrder(req.getColumns());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("HangHoa");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns.get(i).getHeader());
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (ProductInventoryRes product : items) {
                List<ProductVariantInventoryRes> variants = product.getVariants() == null || product.getVariants().isEmpty()
                        ? List.of(ProductVariantInventoryRes.builder().build())
                        : product.getVariants();

                for (ProductVariantInventoryRes variant : variants) {
                    Row row = sheet.createRow(rowIndex++);
                    for (int col = 0; col < columns.size(); col++) {
                        writeCell(row.createCell(col), columns.get(col), product, variant);
                    }
                }
            }

            for (int i = 0; i < columns.size(); i++) {
                sheet.autoSizeColumn(i);
                if (columns.get(i).isTechnical()) {
                    sheet.setColumnHidden(i, true);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }private void writeCell(Cell cell, InventoryExcelColumn column, ProductInventoryRes product, ProductVariantInventoryRes variant) {
        switch (column) {
            case PRODUCT_ID -> cell.setCellValue(numberToString(product.getId()));
            case VARIANT_ID -> cell.setCellValue(numberToString(variant.getVariantId()));
            case INVENTORY_ID -> cell.setCellValue(numberToString(variant.getInventoryId()));
            case PRODUCT_NAME -> cell.setCellValue(nvl(product.getName()));
            case CATEGORY_NAME -> cell.setCellValue(nvl(product.getCategoryName()));
            case BASE_UNIT -> cell.setCellValue(nvl(product.getBaseUnit()));
            case PRODUCT_ACTIVE -> cell.setCellValue(boolToString(product.getActive()));
            case PRODUCT_DESCRIPTION -> cell.setCellValue(nvl(product.getDescription()));
            case SKU -> cell.setCellValue(nvl(variant.getSku()));
            case VARIANT_CODE -> cell.setCellValue(nvl(variant.getVariantCode()));
            case WEIGHT -> cell.setCellValue(nvl(variant.getWeight()));
            case RETAIL_PRICE -> writeNumber(cell, variant.getRetailPrice());
            case STORE_PRICE -> writeNumber(cell, variant.getStorePrice());
            case VARIANT_ACTIVE -> cell.setCellValue(boolToString(variant.getActive()));
            case INVENTORY_CODE -> cell.setCellValue(nvl(variant.getInventoryCode()));
            case ORIGINAL_QTY -> writeNumber(cell, variant.getOriginalQty());
            case REMAINING_QTY -> writeNumber(cell, variant.getRemainingQty());
            case COST_PRICE -> writeNumber(cell, variant.getCostPrice());
            case OUT_OF_STOCK -> cell.setCellValue(boolToString(variant.isOutOfStock()));
        }
    }

    private void writeNumber(Cell cell, Double value) {
        if (value != null) cell.setCellValue(value);
        else cell.setBlank();
    }

    @Transactional
    public InventoryExcelImportRes importInventoryExcel(MultipartFile file) throws IOException {
        InventoryExcelImportRes result = InventoryExcelImportRes.empty();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet("HangHoa");
            if (sheet == null) {
                result.getErrors().add("Không tìm thấy sheet HangHoa.");
                return result;
            }

            Map<String, Integer> headerMap = readHeaderMap(sheet.getRow(0));
            int totalRows = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) continue;
                totalRows++;

                try {
                    importRow(row, headerMap, result);
                } catch (Exception ex) {
                    result.getErrors().add("Dòng " + (i + 1) + ": " + ex.getMessage());
                }
            }

            result.setTotalRows(totalRows);
            return result;
        }
    }

    private void importRow(Row row, Map<String, Integer> headerMap, InventoryExcelImportRes result) {
        Long productId = readLong(row, headerMap, "ID sản phẩm");
        Long variantId = readLong(row, headerMap, "ID biến thể");
        Long inventoryId = readLong(row, headerMap, "ID tồn kho");

        String productName = readString(row, headerMap, "Tên sản phẩm");
        String categoryName = readString(row, headerMap, "Danh mục");
        String baseUnit = readString(row, headerMap, "Đơn vị cơ bản");
        Boolean productActive = readBoolean(row, headerMap, "Sản phẩm đang bán", true);
        String description = readString(row, headerMap, "Mô tả");

        String sku = readString(row, headerMap, "SKU");
        String variantCode = readString(row, headerMap, "Mã biến thể");
        String weight = readString(row, headerMap, "Trọng lượng");
        Double retailPrice = readDouble(row, headerMap, "Giá bán lẻ");
        Double storePrice = readDouble(row, headerMap, "Giá bán buôn");
        Boolean variantActive = readBoolean(row, headerMap, "Biến thể đang bán", true);

        String inventoryCode = readString(row, headerMap, "Mã lô");
        Double originalQty = readDouble(row, headerMap, "SL ban đầu");
        Double remainingQty = readDouble(row, headerMap, "SL tồn");
        Double costPrice = readDouble(row, headerMap, "Giá vốn");

        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Thiếu tên sản phẩm");
        }
        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalArgumentException("Thiếu danh mục");
        }

//        Category category = categoryRepository.findByNameIgnoreCase(categoryName.trim())
//                .orElseGet(() -> categoryRepository.save(Category.builder().name(categoryName.trim()).build()));

        Product product = null;
        if (productId != null) {
            product = productRepository.findById(productId).orElse(null);
        }
        if (product == null && sku != null && !sku.isBlank()) {
            product = productRepository.findFirstByVariants_Sku(sku).orElse(null);
        }
        if (product == null) {
            product = new Product();
            product.setName(productName.trim());
            product.setBaseUnit(blankToDefault(baseUnit, "mét"));
            product.setDescription(description);
            product.setActive(productActive != null ? productActive : true);
//            product.setCategory(category);
            product.setVariants(new ArrayList<>());
            product = productRepository.save(product);
            result.setCreatedProducts(result.getCreatedProducts() + 1);
        } else {
            product.setName(productName.trim());
            product.setBaseUnit(blankToDefault(baseUnit, product.getBaseUnit()));
            product.setDescription(description);
            product.setActive(productActive != null ? productActive : product.isActive());
//            product.setCategory(category);
            product = productRepository.save(product);
            result.setUpdatedProducts(result.getUpdatedProducts() + 1);
        }

        ProductVariant variant = null;
        if (variantId != null) {
            variant = productVariantRepository.findById(variantId).orElse(null);
        }
        if (variant == null && sku != null && !sku.isBlank()) {
            variant = productVariantRepository.findFirstBySku(sku).orElse(null);
        }
        if (variant == null) {
            variant = new ProductVariant();
            variant.setProduct(product);
            variant.setVariantCode(blankToDefault(variantCode, "Mặc định"));
            variant.setSku(sku);
            variant.setWeight(weight);
            variant.setRetailPrice(orZero(retailPrice));
            variant.setStorePrice(orZero(storePrice));
            variant.setActive(variantActive != null ? variantActive : true);
            variant = productVariantRepository.save(variant);
            result.setCreatedVariants(result.getCreatedVariants() + 1);
        } else {
            variant.setProduct(product);
            variant.setVariantCode(blankToDefault(variantCode, variant.getVariantCode()));
            variant.setSku(sku);
            variant.setWeight(weight);
            variant.setRetailPrice(retailPrice != null ? retailPrice : variant.getRetailPrice());
            variant.setStorePrice(storePrice != null ? storePrice : variant.getStorePrice());
            variant.setActive(variantActive != null ? variantActive : variant.isActive());
            variant = productVariantRepository.save(variant);
            result.setUpdatedVariants(result.getUpdatedVariants() + 1);
        }

        if ((inventoryCode != null && !inventoryCode.isBlank()) || inventoryId != null) {
            Inventory lot = null;
            if (inventoryId != null) {
                lot = inventoryRepository.findById(inventoryId).orElse(null);
            }
            if (lot == null && inventoryCode != null && !inventoryCode.isBlank()) {
                lot = inventoryRepository.findFirstByVariant_IdAndInventoryCode(variant.getId(), inventoryCode).orElse(null);
            }
            if (lot == null) {
                lot = new Inventory();
                lot.setVariant(variant);
                lot.setInventoryCode(blankToDefault(inventoryCode, "AUTO"));
                lot.setOriginalQty(orZero(originalQty));
                lot.setRemainingQty(orZero(remainingQty));
                lot.setCostPrice(orZero(costPrice));
//                lot.setOutOfStock(orZero(remainingQty) <= 0);
                lot.setActive(true);
                inventoryRepository.save(lot);
                result.setCreatedLots(result.getCreatedLots() + 1);
            } else {
                lot.setVariant(variant);
                if (inventoryCode != null && !inventoryCode.isBlank()) lot.setInventoryCode(inventoryCode);
                if (originalQty != null) lot.setOriginalQty(originalQty);
                if (remainingQty != null) lot.setRemainingQty(remainingQty);
                if (costPrice != null) lot.setCostPrice(costPrice);
//                lot.setOutOfStock(orZero(lot.getRemainingQty()) <= 0);
                inventoryRepository.save(lot);
                result.setUpdatedLots(result.getUpdatedLots() + 1);
            }
        }
    }
    private Map<String, Integer> readHeaderMap(Row row) {
        Map<String, Integer> map = new HashMap<>();
        if (row == null) return map;
        for (Cell cell : row) {
            map.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
        }
        return map;
    }

    private boolean isEmptyRow(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK && !readCellAsString(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String readString(Row row, Map<String, Integer> headers, String header) {
        Integer index = headers.get(header);
        if (index == null) return null;
        Cell cell = row.getCell(index);
        return cell == null ? null : trimToNull(readCellAsString(cell));
    }

    private Long readLong(Row row, Map<String, Integer> headers, String header) {
        String value = readString(row, headers, header);
        if (value == null) return null;
        return Long.valueOf(value.contains(".") ? value.substring(0, value.indexOf('.')) : value);
    }

    private Double readDouble(Row row, Map<String, Integer> headers, String header) {
        String value = readString(row, headers, header);
        if (value == null) return null;
        return Double.valueOf(value);
    }

    private Boolean readBoolean(Row row, Map<String, Integer> headers, String header, boolean defaultValue) {
        String value = readString(row, headers, header);
        if (value == null) return defaultValue;
        String normalized = value.trim().toLowerCase();
        return normalized.equals("true") || normalized.equals("1") || normalized.equals("x") || normalized.equals("có");
    }

    private String readCellAsString(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double value = cell.getNumericCellValue();
                if (value == Math.rint(value)) yield String.valueOf((long) value);
                yield String.valueOf(value);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    private String boolToString(Boolean value) {
        return Boolean.TRUE.equals(value) ? "TRUE" : "FALSE";
    }

    private String numberToString(Number value) {
        return value == null ? "" : String.valueOf(value.longValue());
    }

    private Double orZero(Double value) {
        return value == null ? 0D : value;
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String blankToDefault(String value, String defaultValue) {
        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }
}
