package com.gener.qlbh.services;

import com.gener.qlbh.dtos.response.ProductExcelImportRes;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.models.Product;
import com.gener.qlbh.models.ProductVariant;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.repositories.ProductRepository;
import com.gener.qlbh.repositories.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductExcelService {

    private static final String SHEET_NAME = "HangHoa";

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public ResponseEntity<ResponseObject> importProductExcel(MultipartFile file) throws APIException {
        if (file == null || file.isEmpty()) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getCode())
                    .message("File excel không được để trống")
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatus())
                    .build();
        }

        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!filename.endsWith(".xlsx")) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getCode())
                    .message("Chỉ hỗ trợ file .xlsx")
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatus())
                    .build();
        }

        try {
            ProductExcelImportRes result = importInternal(file);

            return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode())
                    .body(ResponseObject.builder()
                            .status(SuccessCode.REQUEST.getStatus())
                            .message("Import sản phẩm từ excel thành công")
                            .data(result)
                            .build());

        } catch (Exception e) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getCode())
                    .message("Không thể đọc file excel: " + e.getMessage())
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatus())
                    .build();
        }
    }

    private ProductExcelImportRes importInternal(MultipartFile file) throws IOException {
        ProductExcelImportRes result = ProductExcelImportRes.empty();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }

            if (sheet == null) {
                result.getErrors().add("Không tìm thấy sheet dữ liệu.");
                return result;
            }

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> headers = readHeaderMap(headerRow);

            validateRequiredHeaders(headers);

            int totalRows = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                totalRows++;
                try {
                    importRow(row, headers, result);
                } catch (Exception ex) {
                    result.getErrors().add("Dòng " + (i + 1) + ": " + ex.getMessage());
                    result.setSkippedRows(result.getSkippedRows() + 1);
                }
            }

            result.setTotalRows(totalRows);
            return result;
        }
    }

    private void validateRequiredHeaders(Map<String, Integer> headers) {
        List<String> required = List.of(
                "Tên sản phẩm",
                "Danh mục",
                "Đơn vị cơ bản"
        );

        for (String header : required) {
            if (!headers.containsKey(header)) {
                throw new IllegalArgumentException("Thiếu cột bắt buộc: " + header);
            }
        }
    }

    private void importRow(Row row, Map<String, Integer> headers, ProductExcelImportRes result) {

        String productName = readString(row, headers, "Tên sản phẩm");
        String categoryName = readString(row, headers, "Danh mục");
        String baseUnit = readString(row, headers, "Đơn vị cơ bản");
        String description = readString(row, headers, "Mô tả");

        String sku = readString(row, headers, "SKU");
        String variantCode = readString(row, headers, "Mã biến thể");

        String weight = readString(row, headers, "Trọng lượng");
        Double retailPrice = readDouble(row, headers, "Giá bán lẻ");
        Double storePrice = readDouble(row, headers, "Giá bán buôn");

        if (isBlank(productName)) {
            throw new IllegalArgumentException("Thiếu tên sản phẩm");
        }
        if (isBlank(categoryName)) {
            throw new IllegalArgumentException("Thiếu danh mục");
        }
        if (isBlank(baseUnit)) {
            throw new IllegalArgumentException("Thiếu đơn vị cơ bản");
        }

        Product product = resolveProduct(productName);

        if (product == null) {
            product = Product.builder()
                    .name(productName.trim())
                    .categoryName(categoryName.trim())
                    .baseUnit(baseUnit.trim())
                    .description(trimToNull(description))
//                    .warningQuantity(warningQuantity)
                    .active(true)
                    .variants(new ArrayList<>())
                    .build();

            product = productRepository.save(product);
            result.setCreatedProducts(result.getCreatedProducts() + 1);
        }

        boolean hasVariantData =
                !isBlank(sku) ||
                        !isBlank(variantCode) ||
                        weight != null ||
                        retailPrice != null ||
                        storePrice != null;
        if (!hasVariantData) {
            return;
        }

        ProductVariant variant = resolveVariant(sku, product.getId(), variantCode);

        // CHI THEM MOI, KHONG DE LEN BIEN THE CU
        if (variant == null) {
            variant = ProductVariant.builder()
                    .product(product)
                    .sku(trimToNull(sku))
                    .variantCode(defaultIfBlank(variantCode, "DEFAULT"))
                    .weight(weight)
                    .retailPrice(retailPrice)
                    .storePrice(storePrice)
                    .active(true)
                    .inventories(new ArrayList<>())
                    .build();

            productVariantRepository.save(variant);
            result.setCreatedVariants(result.getCreatedVariants() + 1);
        } else {
            // bỏ qua nếu biến thể đã tồn tại
            result.setSkippedRows(result.getSkippedRows() + 1);
            result.getErrors().add(
                    "Biến thể đã tồn tại"
                            + (variant.getSku() != null ? " - SKU: " + variant.getSku() : "")
            );
        }
    }

    private Product resolveProduct(String productName) {


        if (!isBlank(productName)) {
            return productRepository.findByNameIgnoreCase(productName.trim()).orElse(null);
        }

        return null;
    }

    private ProductVariant resolveVariant(String sku, Long productId, String variantCode) {


        if (!isBlank(sku)) {
            Optional<ProductVariant> bySku = productVariantRepository.findFirstBySkuIgnoreCase(sku.trim());
            if (bySku.isPresent()) {
                return bySku.get();
            }
        }

        if (productId != null && !isBlank(variantCode)) {
            return productVariantRepository
                    .findFirstByProduct_IdAndVariantCodeIgnoreCase(productId, variantCode.trim())
                    .orElse(null);
        }

        return null;
    }

    private Map<String, Integer> readHeaderMap(Row row) {
        Map<String, Integer> map = new HashMap<>();
        if (row == null) return map;

        for (Cell cell : row) {
            String header = trimToNull(readCellAsString(cell));
            if (header != null) {
                map.put(header, cell.getColumnIndex());
            }
        }
        return map;
    }

    private boolean isEmptyRow(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = readCellAsString(cell);
                if (!value.isBlank()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String readString(Row row, Map<String, Integer> headers, String header) {
        Integer index = headers.get(header);
        if (index == null) return null;

        Cell cell = row.getCell(index);
        if (cell == null) return null;

        return trimToNull(readCellAsString(cell));
    }

    private Long readLong(Row row, Map<String, Integer> headers, String header) {
        String value = readString(row, headers, header);
        if (value == null) return null;

        try {
            if (value.contains(".")) {
                value = value.substring(0, value.indexOf('.'));
            }
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cột [" + header + "] không phải số nguyên hợp lệ");
        }
    }

    private Double readDouble(Row row, Map<String, Integer> headers, String header) {
        String value = readString(row, headers, header);
        if (value == null) return null;

        try {
            return Double.parseDouble(value.replace(",", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cột [" + header + "] không phải số hợp lệ");
        }
    }

    private Boolean readBoolean(Row row, Map<String, Integer> headers, String header, boolean defaultValue) {
        String value = readString(row, headers, header);
        if (value == null) return defaultValue;

        String normalized = value.trim().toLowerCase();
        return normalized.equals("true")
                || normalized.equals("1")
                || normalized.equals("x")
                || normalized.equals("có")
                || normalized.equals("yes");
    }

    private String readCellAsString(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double value = cell.getNumericCellValue();
                if (value == Math.rint(value)) {
                    yield String.valueOf((long) value);
                }
                yield String.valueOf(value);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield switch (cell.getCachedFormulaResultType()) {
                        case STRING -> cell.getStringCellValue();
                        case NUMERIC -> {
                            double value = cell.getNumericCellValue();
                            if (value == Math.rint(value)) {
                                yield String.valueOf((long) value);
                            }
                            yield String.valueOf(value);
                        }
                        case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                        default -> "";
                    };
                } catch (Exception ex) {
                    yield "";
                }
            }
            default -> "";
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value.trim();
    }
}