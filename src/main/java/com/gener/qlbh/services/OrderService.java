package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.OrderReq;
import com.gener.qlbh.dtos.request.PaidDeptReq;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.mapper.OrderMapper;
import com.gener.qlbh.models.*;
import com.gener.qlbh.repositories.CustomerRepository;
import com.gener.qlbh.repositories.InventoryRepository;
import com.gener.qlbh.repositories.OrderRepository;
import com.gener.qlbh.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final OrderNumberService orderNumberService;
    private final InventoryRepository inventoryRepository;

    public ResponseEntity<ResponseObject> getAllOrders(){
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get All Orders Successfully")
                        .data(orderRepository.findAll())
                        .build()
        );

    }

    @Transactional
    public ResponseEntity<ResponseObject> getOrderById(String id) throws APIException {
        Order order = orderRepository.findById(id).orElseThrow(()-> APIException.builder()
                .status(ErrorCode.NOT_FOUND.getStatus())
                .message("Cannot Found Order With Id = "+id)
                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                .build());
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get Order With Id = "+id+" Successfully")
                        .data(order)
                        .build()
        );

    }

    @Transactional
    public ResponseEntity<ResponseObject> createOrder(OrderReq req) throws APIException {
        Customer customer = null;
        if (req.getCustomerId()!=null){
            customer = customerRepository.findById(req.getCustomerId()).orElseThrow(()-> APIException.builder()
                    .status(ErrorCode.NOT_FOUND.getStatus())
                    .message("Cannot Found Customer With Id = "+req.getCustomerId())
                    .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                    .build());
        }

//        for (var product:req.getOrderDetailReqs()){
//            if (product.getProductId()!=null){
//                productRepository.findById(product.getProductId()).orElseThrow(()-> APIException.builder()
//                        .status(ErrorCode.NOT_FOUND.getStatus())
//                        .message("Cannot Found Product With Id = "+product.getProductId())
//                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
//                        .build());
//            }
//        }

        Order order = orderMapper.toOrder(req);
        Set<OrderDetail> details = new HashSet<>();
        Double subtotal = 0d;

        for (var orderDetailReq:req.getOrderDetailReqs()){
            Product product =null;
            if(orderDetailReq.getProductId()!=null&&!orderDetailReq.getProductId().isEmpty()&&!orderDetailReq.getProductId().isBlank()){
                product = productRepository.findById(orderDetailReq.getProductId()).orElseThrow(()-> APIException.builder()
                        .status(ErrorCode.NOT_FOUND.getStatus())
                        .message("Cannot Found Product With Id = "+orderDetailReq.getProductId())
                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                        .build());
            }

            OrderDetail orderDetail = orderMapper.toOrderDetail(orderDetailReq);


            orderDetail.setOrder(order);
            orderDetail.setProduct(product);
            orderDetail.setBaseUnit(product==null?orderDetailReq.getBaseUnit():product.getBaseUnit());
            orderDetail.setPrice(orderDetailReq.getPrice());
            if (product!=null){
                Inventory inventory = product.getInventory();
                inventory.setTotalBaseUnitQty(inventory.getTotalBaseUnitQty()-orderDetail.getTotalQuantity());
                inventoryRepository.save(inventory);
            }
            details.add(orderDetail);
            subtotal+=orderDetail.getSubtotal();



        }
        order.setDetails(details);
        order.setSubtotal(subtotal);
        order.setAmount();
        order.setCustomer(customer);
        order.setCreatedAt(req.getCreatedAt()==null? LocalDateTime.now():LocalDateTime.ofInstant(Instant.parse(req.getCreatedAt()), ZoneId.systemDefault()));

        String id = orderNumberService.nextOrderCode();
//        log.info(id);
        order.setId(id);
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
    public ResponseEntity<ResponseObject> deleteOrder(String id){
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()){
            for (var orderDetail:order.get().getDetails()){
                if (orderDetail.getProduct()!=null){
                    Inventory inventory = orderDetail.getProduct().getInventory();
                    inventory.setTotalBaseUnitQty(inventory.getTotalBaseUnitQty()+orderDetail.getTotalQuantity());
                    inventoryRepository.save(inventory);
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
}
