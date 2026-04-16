package com.gener.qlbh.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ================= USER (1000 - 1999) =================
    USER_NOT_FOUND(1001, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    USERNAME_NOT_EXISTS(1002, "Tài khoản không tồn tại", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS(1003, "Email đã tồn tại", HttpStatus.CONFLICT),

    // ================= AUTH (2000 - 2999) =================
    UNAUTHORIZED(2001, "Yêu cầu xác thực người dùng", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(2002, "Bạn không có quyền truy cập", HttpStatus.FORBIDDEN),
    WRONG_PASSWORD(2003, "Tài khoản hoặc mật khẩu chưa chính xác", HttpStatus.UNAUTHORIZED),

    // ================= PRODUCT (3000 - 3099) =================
    PRODUCT_NOT_FOUND(3001, "Không tìm thấy sản phẩm", HttpStatus.NOT_FOUND),
    VARIANT_NOT_FOUND(3002, "Không tìm thấy biến thể sản phẩm", HttpStatus.NOT_FOUND),
    VARIANT_IN_USE(3003, "Không thể xóa biến thể vì đã được sử dụng trong đơn hàng", HttpStatus.BAD_REQUEST),

    PRODUCT_ID_REQUIRED(3004, "ProductId là bắt buộc", HttpStatus.BAD_REQUEST),
    PRODUCT_VARIANT_ID_REQUIRED(3005, "ProductVariantId là bắt buộc", HttpStatus.BAD_REQUEST),
    PRODUCT_IN_USE(3006, "Sản phẩm đang được sử dụng, không thể xóa", HttpStatus.BAD_REQUEST),

    // ================= INVENTORY (3100 - 3199) =================
    INVENTORY_NOT_FOUND(3101, "Không tìm thấy tồn kho", HttpStatus.NOT_FOUND),
    INVENTORY_CODE_ALREADY_EXISTS(3102, "Mã tồn kho đã tồn tại", HttpStatus.CONFLICT),

    // ================= PURCHASE RECEIPT (3200 - 3299) =================
    PURCHASE_RECEIPT_NOT_FOUND(3201, "Không tìm thấy phiếu nhập", HttpStatus.NOT_FOUND),
    PURCHASE_RECEIPT_TOTAL_QUANTITY_INVALID(3202, "Số lượng nhập phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    PURCHASE_RECEIPT_COST_INVALID(3203, "Giá nhập phải lớn hơn hoặc bằng 0", HttpStatus.BAD_REQUEST),
    PURCHASE_RECEIPT_QUANTITY_INVALID(3204, "Số lượng phiếu nhập không hợp lệ", HttpStatus.BAD_REQUEST),
    PURCHASE_RECEIPT_INVENTORY_NOT_FOUND(3205, "Phiếu nhập chưa liên kết tồn kho", HttpStatus.BAD_REQUEST),
    PURCHASE_RECEIPT_DELETE_STOCK_NOT_ENOUGH(3206, "Không thể xóa phiếu nhập vì tồn kho hiện tại nhỏ hơn số lượng phiếu nhập", HttpStatus.BAD_REQUEST),

    // ================= INVOICE (4000 - 4099) =================
    INVOICE_ALREADY_PAID(4001, "Hóa đơn này đã được thanh toán hoàn tất", HttpStatus.BAD_REQUEST),
    INVOICE_NOT_FOUND(4002, "Không tìm thấy hóa đơn", HttpStatus.NOT_FOUND),

    // ================= PAYMENT (4100 - 4199) =================
    INVALID_PAYMENT_AMOUNT(4101, "Số tiền thanh toán phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    PAYMENT_EXCEED_TOTAL(4102, "Số tiền thanh toán vượt quá công nợ", HttpStatus.BAD_REQUEST),

    // ================= COMMON (8000 - 8999) =================
    BAD_REQUEST(8000, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    NOT_FOUND(8001, "Không tìm thấy dữ liệu", HttpStatus.NOT_FOUND),
    CONFLICT(8002, "Dữ liệu bị xung đột", HttpStatus.CONFLICT),
    UNPROCESSABLE_ENTITY(8003, "Không thể xử lý dữ liệu", HttpStatus.UNPROCESSABLE_ENTITY),
    DATA_IN_USE(8004, "Dữ liệu đang được sử dụng, không thể xóa", HttpStatus.BAD_REQUEST),

    // ================= SYSTEM (9000 - 9999) =================
    INTERNAL_SERVER_ERROR(9000, "Đã xảy ra lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),
    CANNOT_READ_IMAGE(9001, "Không thể đọc ảnh", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}