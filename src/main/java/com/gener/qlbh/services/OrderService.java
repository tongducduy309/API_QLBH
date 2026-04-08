package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.OrderCreateReq;
import com.gener.qlbh.dtos.request.OrderDetailCreateReq;
import com.gener.qlbh.dtos.request.OrderDetailUpdateReq;
import com.gener.qlbh.dtos.request.OrderUpdateReq;
import com.gener.qlbh.dtos.request.PaidDeptReq;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.OrderStatus;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.mapper.CustomerMapper;
import com.gener.qlbh.mapper.OrderMapper;
import com.gener.qlbh.models.Customer;
import com.gener.qlbh.models.InventoryLot;
import com.gener.qlbh.models.Order;
import com.gener.qlbh.models.OrderDetail;
import com.gener.qlbh.models.ProductVariant;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.repositories.CustomerRepository;
import com.gener.qlbh.repositories.InventoryRepository;
import com.gener.qlbh.repositories.OrderDetailRepository;
import com.gener.qlbh.repositories.OrderRepository;
import com.gener.qlbh.repositories.ProductVariantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final OrderMapper orderMapper;
    private final OrderNumberService orderNumberService;
    private final InventoryRepository inventoryRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductVariantRepository productVariantRepository;

    public ResponseEntity<ResponseObject> getAllOrders() {
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get All Orders Successfully")
                        .data(orderMapper.toOrderRes(orderRepository.findAll()))
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> getOrderById(Long id) throws APIException {
        Order order = findOrderByIdOrThrow(id);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get Order With Id = " + id + " Successfully")
                        .data(orderMapper.toOrderRes(order))
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> createOrder(OrderCreateReq req) throws APIException {
        Customer customer = getCustomerOrNull(req.getCustomerId());

        Order order = orderMapper.toOrder(req);
        order.setCustomer(customer);
        order.setCreatedAt(parseCreatedAt(req.getCreatedAt()));
        order.setCode(orderNumberService.nextOrderCode());

        Set<OrderDetail> details = new HashSet<>();
        double subtotal = 0d;

        if (req.getOrderDetailCreateReqs() != null) {
            for (OrderDetailCreateReq dReq : req.getOrderDetailCreateReqs()) {
                ProductVariant variant = getVariantOrNull(dReq.getProductVariantId());
                InventoryLot inventory = getInventoryOrNull(dReq.getInventoryId());

                OrderDetail detail = orderMapper.toOrderDetail(dReq);
                detail.setOrder(order);
                detail.setProductVariant(variant);
                detail.setInventory(inventory);
                detail.setPrice(dReq.getPrice());
                detail.setLineIndex(dReq.getLineIndex());

                if (shouldAffectInventory(req.getStatus()) && variant != null && inventory!=null) {
                    deductInventory(inventory, detail.getTotalQuantity(), dReq.getName());
                }

                details.add(detail);
                subtotal += safeDouble(detail.getSubtotal());
            }
        }

        order.setDetails(details);
        order.setSubtotal(subtotal);
        order.setAmount();

        Order saved = orderRepository.save(order);

        return ResponseEntity.status(SuccessCode.CREATE.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.CREATE.getStatus())
                        .message("Create Order Successfully")
                        .data(orderMapper.toOrderRes(saved))
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> getNextOrderCode() {
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get Next Order Code")
                        .data(orderNumberService.getNextOrderCode())
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> updateOrder(Long id, OrderUpdateReq req) throws APIException {
        Order order = findOrderByIdOrThrow(id);

        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = req.getStatus();

        boolean oldAffectInventory = shouldAffectInventory(oldStatus);
        boolean newAffectInventory = shouldAffectInventory(newStatus);

        Customer customer = getCustomerOrNull(req.getCustomerId());

        if (oldAffectInventory) {
            rollbackInventoryForOrder(order);
        }

        order.getDetails().clear();
        orderDetailRepository.flush();

        Set<OrderDetail> newDetails = new HashSet<>();
        double subtotal = 0d;

        if (req.getOrderDetailUpdateReqs() != null) {
            for (OrderDetailUpdateReq dReq : req.getOrderDetailUpdateReqs()) {
                ProductVariant variant = getVariantOrNull(dReq.getProductVariantId());
                InventoryLot inventory = getInventoryOrNull(dReq.getInventoryId());

                OrderDetail detail = new OrderDetail();
                detail.setOrder(order);
                detail.setName(dReq.getName());
                detail.setPrice(dReq.getPrice());
                detail.setQuantity(dReq.getQuantity());
                detail.setLength(dReq.getLength());
                detail.setBaseUnit(dReq.getBaseUnit());
                detail.setLineIndex(dReq.getLineIndex());
                detail.setKind(dReq.getKind());
                detail.setProductVariant(variant);
                detail.setInventory(inventory);

                if (newAffectInventory && variant != null) {
                    deductInventory(inventory, detail.getTotalQuantity(), dReq.getName());
                }

                newDetails.add(detail);
                subtotal += safeDouble(detail.getSubtotal());
            }
        }

        order.setCustomer(customer);
        order.setStatus(newStatus);
        order.setCreatedAt(parseCreatedAt(req.getCreatedAt()));
        order.setDetails(newDetails);
        order.setSubtotal(subtotal);
        order.setAmount();

        Order saved = orderRepository.save(order);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Update Order Successfully")
                        .data(orderMapper.toOrderRes(saved))
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> deleteOrder(Long id) throws APIException {
        Order order = findOrderByIdOrThrow(id);

        if (shouldAffectInventory(order.getStatus())) {
            rollbackInventoryForOrder(order);
        }

        orderRepository.delete(order);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(
                        SuccessCode.REQUEST.getStatus(),
                        "Delete Order Successfully",
                        ""
                )
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> paidDeptOrder(PaidDeptReq req) throws APIException {
        Order order = findOrderByIdOrThrow(req.getOrderId());

        if (order.getRemainingAmount() == null || order.getRemainingAmount() <= 0) {
            throw APIException.builder()
                    .status(ErrorCode.NOT_FOUND.getStatus())
                    .message("Hóa đơn này đã được trả hoàn tất")
                    .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                    .build();
        }

        double requestPaid = safeDouble(req.getPaidDept());
        if (requestPaid <= 0) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getStatus())
                    .message("Số tiền thanh toán phải lớn hơn 0")
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatusCode())
                    .build();
        }

        double currentRemaining = safeDouble(order.getRemainingAmount());
        double currentPaid = safeDouble(order.getPaidDept());
        double actualPaid = Math.min(requestPaid, currentRemaining);

        order.setPaidDept(currentPaid + actualPaid);
        order.setRemainingAmount(currentRemaining - actualPaid);

        Order saved = orderRepository.save(order);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Đã thanh toán hóa đơn thành công với số tiền: " + actualPaid)
                        .data(saved)
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> getDeptOrderByCustomerId(Long customerId) throws APIException {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> APIException.builder()
                .status(ErrorCode.NOT_FOUND.getStatus())
                .message("Cannot Found Customer With Id = " + customerId)
                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                .build());

        List<Order> orders = orderRepository.findByCustomer_IdAndRemainingAmountGreaterThanOrderByCreatedAtDesc(customerId, 0);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Danh sách hóa đơn còn công nợ")
                        .data(orders)
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> getOrdersRecent(Long amount) {
        List<Order> orders = orderRepository.findOrdersRecent(amount);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Danh sách hóa đơn gần đây")
                        .data(orderMapper.toOrderRecentRes(orders))
                        .build()
        );
    }

    private Order findOrderByIdOrThrow(Long id) throws APIException {
        return orderRepository.findById(id).orElseThrow(() -> APIException.builder()
                .status(ErrorCode.NOT_FOUND.getStatus())
                .message("Cannot Found Order With Id = " + id)
                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                .build());
    }

    private Customer getCustomerOrNull(Long customerId) throws APIException {
        if (customerId == null) {
            return null;
        }

        return customerRepository.findById(customerId).orElseThrow(() -> APIException.builder()
                .status(ErrorCode.NOT_FOUND.getStatus())
                .message("Cannot Found Customer With Id = " + customerId)
                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                .build());
    }

    private ProductVariant getVariantOrNull(Long variantId) throws APIException {
        if (variantId == null) {
            return null;
        }

        return productVariantRepository.findById(variantId).orElseThrow(() -> APIException.builder()
                .status(ErrorCode.NOT_FOUND.getStatus())
                .message("Cannot Found Product Variant With Id = " + variantId)
                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                .build());
    }

    private InventoryLot getInventoryOrNull(Long inventoryId) throws APIException {
        if (inventoryId == null) {
            return null;
        }

        return inventoryRepository.findById(inventoryId).orElseThrow(() -> APIException.builder()
                .status(ErrorCode.NOT_FOUND.getStatus())
                .message("Cannot Found Inventory With Id = " + inventoryId)
                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                .build());
    }

    private boolean shouldAffectInventory(OrderStatus status) {
        return status == OrderStatus.CONFIRMED;
    }

    private void deductInventory(InventoryLot inventory, double quantity, String productName) throws APIException {
        if (inventory == null) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getStatus())
                    .message("Không tìm thấy lô tồn kho cho sản phẩm: " + (productName == null ? "" : productName))
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatusCode())
                    .build();
        }

        double remainingQty = safeDouble(inventory.getRemainingQty());

        if (quantity <= 0) {
            return;
        }

        if (remainingQty < quantity) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getStatus())
                    .message("Số lượng tồn kho không đủ cho sản phẩm: " + (productName == null ? "" : productName))
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatusCode())
                    .build();
        }

        inventory.setRemainingQty(remainingQty - quantity);
        inventoryRepository.save(inventory);
    }

    private void addBackInventory(InventoryLot inventory, double quantity) {
        if (inventory == null || quantity <= 0) {
            return;
        }

        double remainingQty = safeDouble(inventory.getRemainingQty());
        inventory.setRemainingQty(remainingQty + quantity);
        inventoryRepository.save(inventory);
    }

    private void rollbackInventoryForOrder(Order order) {
        if (order.getDetails() == null || order.getDetails().isEmpty()) {
            return;
        }

        for (OrderDetail detail : order.getDetails()) {
            if (detail.getProductVariant() != null && detail.getInventory() != null) {
                addBackInventory(detail.getInventory(), detail.getTotalQuantity());
            }
        }
    }

    private LocalDate parseCreatedAt(String createdAt) {
        if (createdAt == null || createdAt.isBlank()) {
            return LocalDate.now();
        }

        return LocalDate.ofInstant(
                Instant.parse(createdAt),
                ZoneId.systemDefault()
        );
    }

    private double safeDouble(Double value) {
        return value == null ? 0d : value;
    }
}