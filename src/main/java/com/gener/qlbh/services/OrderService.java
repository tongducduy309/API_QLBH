package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.*;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.OrderStatus;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.mapper.CustomerMapper;
import com.gener.qlbh.mapper.OrderMapper;
import com.gener.qlbh.models.*;
import com.gener.qlbh.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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

    public ResponseEntity<ResponseObject> getAllOrders(){
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
        Order order = orderRepository.findById(id).orElseThrow(()-> APIException.builder()
                .status(ErrorCode.NOT_FOUND.getStatus())
                .message("Cannot Found Order With Id = "+id)
                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                .build());

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get Order With Id = "+id+" Successfully")
                        .data(orderMapper.toOrderRes(order))
                        .build()
        );

    }

    @Transactional
    public ResponseEntity<ResponseObject> createOrder(OrderCreateReq req) throws APIException {
        Customer customer = null;
        if (req.getCustomerId()!=null){
            customer = customerRepository.findById(req.getCustomerId()).orElseThrow(()-> APIException.builder()
                    .status(ErrorCode.NOT_FOUND.getStatus())
                    .message("Cannot Found Customer With Id = "+req.getCustomerId())
                    .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                    .build());
        }

        Order order = orderMapper.toOrder(req);
        Set<OrderDetail> details = new HashSet<>();
        Double subtotal = 0d;

        for (var orderDetailReq:req.getOrderDetailCreateReqs()){
            ProductVariant productVariant =null;
            if(orderDetailReq.getProductVariantId()!=null){
                productVariant = productVariantRepository.findById(orderDetailReq.getProductVariantId()).orElseThrow(()-> APIException.builder()
                        .status(ErrorCode.NOT_FOUND.getStatus())
                        .message("Cannot Found Product Variant With Id = "+orderDetailReq.getProductVariantId())
                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                        .build());
            }

            OrderDetail orderDetail = orderMapper.toOrderDetail(orderDetailReq);


            orderDetail.setOrder(order);
            orderDetail.setProductVariant(productVariant);
            orderDetail.setBaseUnit(orderDetail.getBaseUnit());
            orderDetail.setPrice(orderDetailReq.getPrice());
            orderDetail.setLineIndex(orderDetailReq.getLineIndex());
            if (productVariant!=null && req.getStatus()!= OrderStatus.DRAFT){
                Optional<InventoryLot> inventory = inventoryRepository.findById(orderDetailReq.getInventoryId());
                if (inventory.isPresent()){
                    inventory.get().deduct(orderDetail.getTotalQuantity());
                    inventoryRepository.save(inventory.get());
                    orderDetail.setInventory(inventory.get());
                }

            }

            details.add(orderDetail);
            subtotal+=orderDetail.getSubtotal();



        }
        order.setDetails(details);
        order.setSubtotal(subtotal);
        order.setAmount();
        order.setCustomer(customer);
        order.setCreatedAt(req.getCreatedAt()==null? LocalDate.now():LocalDate.ofInstant(Instant.parse(req.getCreatedAt()), ZoneId.systemDefault()));

        String code = orderNumberService.nextOrderCode();
        order.setCode(code);

        orderRepository.save(order);

        return ResponseEntity.status(SuccessCode.CREATE.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.CREATE.getStatus())
                        .message("Create Order Successfully")
                        .data(orderMapper.toOrderRes(order))
                        .build()
        );

    }

    @Transactional
    public ResponseEntity<ResponseObject> getNextOrderCode(){
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

    /* =========================
       1️⃣ LOAD ORDER
       ========================= */
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> APIException.builder()
                        .status(ErrorCode.NOT_FOUND.getStatus())
                        .message("Cannot Found Order With Id = " + id)
                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                        .build());

    /* =========================
       2️⃣ LOAD CUSTOMER (OPTIONAL)
       ========================= */
        Customer customer = null;
        if (req.getCustomerId() != null) {
            customer = customerRepository.findById(req.getCustomerId())
                    .orElseThrow(() -> APIException.builder()
                            .status(ErrorCode.NOT_FOUND.getStatus())
                            .message("Cannot Found Customer With Id = " + req.getCustomerId())
                            .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                            .build());
        }

    /* =========================
       3️⃣ MAP DETAIL CŨ THEO ID
       ========================= */
        Map<Long, OrderDetail> oldDetailMap = order.getDetails().stream()
                .filter(d -> d.getId() != null)
                .collect(Collectors.toMap(OrderDetail::getId, d -> d));

        Set<OrderDetail> newDetails = new HashSet<>();
        double subtotal = 0d;

    /* =========================
       4️⃣ HANDLE UPDATE / CREATE
       ========================= */
        for (OrderDetailUpdateReq dReq : req.getOrderDetailUpdateReqs()) {

            OrderDetail detail;
            double oldQty = 0;

            /* ===== UPDATE DÒNG CŨ ===== */
            if (dReq.getId() != null && oldDetailMap.containsKey(dReq.getId())) {
                detail = oldDetailMap.get(dReq.getId());
                oldQty = detail.getTotalQuantity();
            }
            /* ===== TẠO DÒNG MỚI ===== */
            else {
                detail = new OrderDetail();
                detail.setOrder(order);
            }

            /* ===== MAP FIELD CƠ BẢN ===== */
            detail.setName(dReq.getName());
            detail.setPrice(dReq.getPrice());
            detail.setQuantity(dReq.getQuantity());
            detail.setLength(dReq.getLength());
            detail.setBaseUnit(dReq.getBaseUnit());
            detail.setLineIndex(dReq.getLineIndex());
            detail.setKind(dReq.getKind());
            /* ===== PRODUCT VARIANT ===== */
            ProductVariant variant = null;
            if (dReq.getProductVariantId() != null) {
                variant = productVariantRepository.findById(dReq.getProductVariantId())
                        .orElseThrow(() -> APIException.builder()
                                .status(ErrorCode.NOT_FOUND.getStatus())
                                .message("Cannot Found Product Variant With Id = " + dReq.getProductVariantId())
                                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                                .build());
            }
            detail.setProductVariant(variant);

            /* ===== TÍNH CHÊNH LỆCH SỐ LƯỢNG ===== */
            double newQty = detail.getTotalQuantity();
            double deltaQty = newQty - oldQty;

            /* ===== XỬ LÝ TỒN KHO ===== */
            if (variant != null && dReq.getInventoryId() != null && deltaQty != 0) {

                InventoryLot inv = inventoryRepository.findById(dReq.getInventoryId())
                        .orElseThrow(() -> APIException.builder()
                                .status(ErrorCode.NOT_FOUND.getStatus())
                                .message("Cannot Found Inventory With Id = " + dReq.getInventoryId())
                                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                                .build());

                if (deltaQty > 0) {
                    if (inv.getRemainingQty() > deltaQty) {
//                        inv.subQuantity(deltaQty);
                    }

                } else {
//                    inv.addQuantity(-deltaQty);
                }

                inventoryRepository.save(inv);
                detail.setInventory(inv);
            }

            newDetails.add(detail);
            subtotal += detail.getSubtotal();
        }

    /* =========================
       5️⃣ HANDLE DELETE DETAIL
       ========================= */
        for (OrderDetail old : order.getDetails()) {
            if (old.getId() != null &&
                    newDetails.stream().noneMatch(d -> Objects.equals(d.getId(), old.getId()))
            ) {
                if (old.getProductVariant() != null && old.getInventory() != null) {
                    inventoryRepository.findById(old.getInventory().getId())
                            .ifPresent(inv -> {
//                                inv.addQuantity(old.getTotalQuantity());
                                inventoryRepository.save(inv);
                            });
                }
            }
        }

    /* =========================
       6️⃣ APPLY DETAIL SET
       ========================= */
        order.getDetails().clear();
        order.getDetails().addAll(newDetails);

    /* =========================
       7️⃣ UPDATE ORDER INFO
       ========================= */
        order.setSubtotal(subtotal);
        order.setAmount();
        order.setCustomer(customer);

        order.setCreatedAt(
                req.getCreatedAt() == null
                        ? LocalDate.now()
                        : LocalDate.ofInstant(
                        Instant.parse(req.getCreatedAt()),
                        ZoneId.systemDefault()
                )
        );

        Order saved = orderRepository.save(order);

    /* =========================
       8️⃣ RESPONSE
       ========================= */
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode())
                .body(ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Update Order Successfully")
                        .data(orderMapper.toOrderRes(saved))
                        .build());
    }


    @Transactional
    public ResponseEntity<ResponseObject> deleteOrder(Long id){
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()){
            for (var orderDetail:order.get().getDetails()){
                if (orderDetail.getProductVariant()!=null){
                    Optional<InventoryLot> inventory = inventoryRepository.findById(orderDetail.getInventory().getId());
                    if (inventory.isPresent()){
//                        inventory.get().addQuantity(orderDetail.getTotalQuantity());
                        inventoryRepository.save(inventory.get());
                    }
                }
            }
            orderRepository.deleteById(id);

        }
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(SuccessCode.REQUEST.getStatus(), "Delete Order Successfully","")
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> paidDeptOrder(PaidDeptReq req) throws APIException {
        Order order = orderRepository.findById(req.getOrderId()).orElseThrow(()-> APIException.builder()
                .status(ErrorCode.NOT_FOUND.getStatus())
                .message("Cannot Found Order With Id = "+req.getOrderId())
                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                .build());
        if (order.getRemainingAmount()==0){
            throw APIException.builder()
                    .status(ErrorCode.NOT_FOUND.getStatus())
                    .message("Hóa đơn này đã được trả hoàn tất")
                    .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                    .build();
        }

        Double paidDept = req.getPaidDept()> order.getRemainingAmount()?order.getRemainingAmount():req.getPaidDept();

        order.setPaidDept(paidDept);
        order.setRemainingAmount(order.getRemainingAmount()-paidDept);
        orderRepository.save(order);
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Đã thanh toán hóa đơn thành công với số tiền: "+req.getPaidDept())
                        .data(order)
                        .build()
        );

    }

    @Transactional
    public ResponseEntity<ResponseObject> getDeptOrderByCustomerId(Long customerId) throws APIException{
        Customer customer = customerRepository.findById(customerId).orElseThrow(()-> APIException.builder()
                .status(ErrorCode.NOT_FOUND.getStatus())
                .message("Cannot Found Customer With Id = "+customerId)
                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                .build());

        List<Order> orders = orderRepository.findByCustomer_IdAndRemainingAmountGreaterThanOrderByCreatedAtDesc(customerId,0);
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Danh sách hóa đơn còn công nợ")
                        .data(orders)
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> getOrdersRecent(Long amount){
        List<Order> orders = orderRepository.findOrdersRecent(amount);
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Danh sách hóa đơn còn công nợ")
                        .data(orderMapper.toOrderRecentRes(orders))
                        .build()
        );
    }
}
