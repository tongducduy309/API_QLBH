package com.gener.qlbh.database;

import com.gener.qlbh.dtos.request.OrderDetailReq;
import com.gener.qlbh.dtos.request.OrderReq;
import com.gener.qlbh.dtos.request.ProductCreateReq;
import com.gener.qlbh.dtos.request.PurchaseOrderCreateReq;
import com.gener.qlbh.enums.Method;
import com.gener.qlbh.models.*;
import com.gener.qlbh.repositories.*;
import com.gener.qlbh.services.OrderService;
import com.gener.qlbh.services.ProductService;
import com.gener.qlbh.services.PurchaseOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Configuration
@Slf4j
public class database {
    @Bean
    CommandLineRunner initDatabase(CategoryRepository categoryRepository, ProductRepository productRepository, ProductService productService, CustomerRepository customerRepository,
                                   OrderRepository orderRepository, OrderService orderService, PurchaseOrderService purchaseOrderService
                                   ){
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {

                Category category = Category.builder()
                        .name("Tôn")
                        .method(Method.SHEET_METAL)
                        .defaultBaseUnit("m")
                        .build();
                categoryRepository.save(category);
                ProductCreateReq productCreateReq = ProductCreateReq.builder()
                        .name("Tôn Lạnh 0.40mm")
                        .retailPrice(160000.0)
                        .storePrice(155000.0)
                        .baseUnit("m")
                        .status(true)
                        .categoryId(category.getId())

                        .build();
                ResponseEntity<ResponseObject> res = productService.createProduct(productCreateReq);
                Long productId;
                String nameProduct;
                try {
                    // 1. ÉP KIỂU TRỰC TIẾP thành Product model
                    //    (Đảm bảo import đúng: com.gener.qlbh.models.Product)
                    Product createdProduct = (Product) res.getBody().getData();

                    // 2. Lấy ID từ đối tượng Product đã ép kiểu
                    productId = createdProduct.getId();
                    nameProduct = createdProduct.getName();

                    System.out.println("-> Đã tạo Product ID: " + productId);
                } catch (Exception e) {
                    System.err.println("LỖI TRÍCH XUẤT PRODUCT ID: " + e.getMessage());
                    // In stack trace để debug thêm nếu cần
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
                OrderDetailReq orderDetailReqA = OrderDetailReq.builder()
                        .productId(productId)
                        .length(2d)
                        .quantity(5d)
                        .price(160000d)
                        .name(nameProduct)
                        .build();
                OrderDetailReq orderDetailReqB = OrderDetailReq.builder()
                        .quantity(1d)
                        .price(20000d)
                        .name("Công uốn")
                        .build();
                List<OrderDetailReq> orderDetailReqs = new ArrayList<>();
                orderDetailReqs.add(orderDetailReqA);
                orderDetailReqs.add(orderDetailReqB);
                OrderReq order = OrderReq.builder()
                        .note("Giao sáng mai, liên hệ trước 30 phút.")
                        .paidAmount(50000d)
                        .shippingFee(10000d)
                        .orderDetailReqs(orderDetailReqs)
                        .customerId(customer.getId())
                        .build();
                orderService.createOrder(order);
                PurchaseOrderCreateReq purchaseOrderCreateReq = PurchaseOrderCreateReq.builder()
                        .productId(productId)
                        .totalLength(500d)
                        .costPerUnit(80000d)
                        .supplier("Dong A")
                        .build();
                purchaseOrderService.createPurchaseOrder(purchaseOrderCreateReq);
//                Product product = Product.builder()
//                        .name("Tôn Lạnh 0.40mm")
//                        .retailPrice(160000.0)
//                        .storePrice(155000.0)
//                        .baseUnit("m")
//                        .category(category)
//                        .build();
//                productRepository.save(product);


            }
        };
    }
}
