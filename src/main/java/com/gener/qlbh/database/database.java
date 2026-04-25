package com.gener.qlbh.database;

import com.gener.qlbh.dtos.request.*;
import com.gener.qlbh.enums.EmployeeStatus;
import com.gener.qlbh.enums.OrderDetailKind;
import com.gener.qlbh.enums.OrderStatus;
import com.gener.qlbh.enums.Role;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.models.*;
import com.gener.qlbh.repositories.*;
import com.gener.qlbh.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Configuration
@Slf4j
public class database {

    @Bean
    CommandLineRunner initDatabase(
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            ProductService productService,
            CustomerRepository customerRepository,
            OrderRepository orderRepository,
            OrderService orderService,
            PurchaseReceiptsService purchaseReceiptsService,
            ProductVariantService productVariantService,
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmployeeService employeeService
    ) {
        return args -> {

            // =========================
            // SEED USER + EMPLOYEE
            // =========================
            seedEmployee(
                    employeeService,
                    userRepository,
                    "Quản trị viên hệ thống",
                    "0900000001",
                    "TP.HCM",
                    "Admin",
                    LocalDate.of(2025, 1, 1),
                    20000000D,
                    "admin_nv",

                    "admin@qlbh.com",
                    Set.of(Role.ADMIN)
            );

            seedEmployee(
                    employeeService,
                    userRepository,
                    "Nguyễn Văn Quản Lý",
                    "0900000002",
                    "TP.HCM",
                    "Quản lý cửa hàng",
                    LocalDate.of(2025, 2, 1),
                    15000000D,
                    "manager01",
                    "manager@qlbh.com",
                    Set.of(Role.STORE_MANAGER)
            );

            seedEmployee(
                    employeeService,
                    userRepository,
                    "Trần Thị Văn Phòng",
                    "0900000003",
                    "TP.HCM",
                    "Nhân viên văn phòng",
                    LocalDate.of(2025, 3, 1),
                    10000000D,
                    "office01",

                    "office@qlbh.com",
                    Set.of(Role.OFFICE_STAFF)
            );

            seedEmployee(
                    employeeService,
                    userRepository,
                    "Lê Văn Giao Hàng",
                    "0900000004",
                    "TP.HCM",
                    "Chạy máy kiêm giao hàng",
                    LocalDate.of(2025, 4, 1),
                    12000000D,
                    "delivery01",
                    "delivery@qlbh.com",
                    Set.of(Role.OPERATOR_DELIVERY)
            );

            // =========================
            // GIỮ NGUYÊN PHẦN DỮ LIỆU CŨ
            // =========================
            ProductCreateReq productCreateReq = ProductCreateReq.builder()
                    .name("Tôn Lạnh")
                    .baseUnit("m")
                    .active(true)
                    .description("Tôn lạnh (hay còn gọi là tôn mạ nhôm kẽm) là một loại vật liệu kim loại được sử dụng rất phổ biến trong xây dựng và công nghiệp nhờ vào khả năng chống ăn mòn và phản xạ nhiệt vượt trội.")
                    .categoryName("Tôn")
                    .build();

            ResponseEntity<ResponseObject> res = productService.createProduct(productCreateReq);
            Long productId;
            String nameProduct;

            try {
                Product createdProduct = (Product) res.getBody().getData();
                productId = createdProduct.getId();
                nameProduct = createdProduct.getName();

                System.out.println("-> Đã tạo Product ID: " + productId);
            } catch (Exception e) {
                System.err.println("LỖI TRÍCH XUẤT PRODUCT ID: " + e.getMessage());
                e.printStackTrace();
                System.err.println("Dừng khởi tạo Order.");
                return;
            }

            Customer customer = Customer.builder()
                    .name("Test")
                    .phone("0586152003")
                    .address("HCM")
                    .build();
            customerRepository.save(customer);

            ProductVariantCreateReq vReq = ProductVariantCreateReq.builder()
                    .productId(productId)
                    .variantCode("0.40mm")
                    .sku("TL4DA")
                    .retailPrice(160000.0)
                    .storePrice(155000.0)
                    .weight("4.2kg")
                    .build();

            ResponseEntity<ResponseObject> vRes = productVariantService.createVariant(vReq);
            ProductVariant createdVariant = (ProductVariant) vRes.getBody().getData();
            Long variantId = createdVariant.getId();

            PurchaseReceiptsCreateReq purchaseReceiptsCreateReq = PurchaseReceiptsCreateReq.builder()
                    .productVariantId(variantId)
                    .totalQuantity(500d)
                    .cost(80000d)
                    .totalCost(40000000d)
                    .supplier("Dong A")
                    .build();
            purchaseReceiptsService.createPurchaseReceipts(purchaseReceiptsCreateReq);

            OrderDetailCreateReq orderDetailCreateReqA = OrderDetailCreateReq.builder()
                    .productVariantId(variantId)
                    .length(2.5d)
                    .quantity(5d)
                    .price(160000d)
                    .name(nameProduct)
                    .inventoryId(1L)
                    .baseUnit("m")
                    .kind(OrderDetailKind.INVENTORY)
                    .build();

            OrderDetailCreateReq orderDetailCreateReqB = OrderDetailCreateReq.builder()
                    .quantity(1d)
                    .price(20000d)
                    .name("Công uốn")
                    .kind(OrderDetailKind.EXPENSE)
                    .build();

            OrderDetailCreateReq orderDetailCreateReqC = OrderDetailCreateReq.builder()
                    .quantity(1d)
                    .price(20000d)
                    .name("Công uốn")
                    .kind(OrderDetailKind.EXPENSE)
                    .build();

            OrderDetailCreateReq orderDetailCreateReqD = OrderDetailCreateReq.builder()
                    .quantity(1d)
                    .price(20000d)
                    .name("Công uốn")
                    .kind(OrderDetailKind.EXPENSE)
                    .build();

            OrderDetailCreateReq orderDetailCreateReqE = OrderDetailCreateReq.builder()
                    .quantity(1d)
                    .price(20000d)
                    .name("Công uốn")
                    .kind(OrderDetailKind.EXPENSE)
                    .build();

            List<OrderDetailCreateReq> orderDetailCreateReqs = new ArrayList<>();
            orderDetailCreateReqs.add(orderDetailCreateReqA);
            orderDetailCreateReqs.add(orderDetailCreateReqB);
            orderDetailCreateReqs.add(orderDetailCreateReqB);
            orderDetailCreateReqs.add(orderDetailCreateReqC);
            orderDetailCreateReqs.add(orderDetailCreateReqD);
            orderDetailCreateReqs.add(orderDetailCreateReqE);

            OrderCreateReq order = OrderCreateReq.builder()
                    .note("Giao sáng mai, liên hệ trước 30 phút.")
                    .paidAmount(50000d)
                    .shippingFee(10000d)
                    .orderDetailCreateReqs(orderDetailCreateReqs)
                    .customerId(customer.getId())
                    .status(OrderStatus.CONFIRMED)
                    .build();

            orderService.createOrder(order);
        };
    }

    private void seedEmployee(
            EmployeeService employeeService,
            UserRepository userRepository,
            String fullName,
            String phone,
            String address,
            String position,
            LocalDate hireDate,
            Double baseSalary,
            String username,
            String email,
            Set<Role> roles
    ) throws APIException {
        if (userRepository.existsByUsername(username)) {
            return;
        }

        EmployeeCreateReq req = EmployeeCreateReq.builder()
                .fullName(fullName)
                .phone(phone)
                .address(address)
                .position(position)
                .hireDate(hireDate)
                .baseSalary(baseSalary)
                .username(username)
                .email(email)
                .role(roles.iterator().next()) // nếu chỉ 1 role
                .build();

        employeeService.createEmployee(req);

        log.info("Seed employee success: {}", fullName);
    }
}