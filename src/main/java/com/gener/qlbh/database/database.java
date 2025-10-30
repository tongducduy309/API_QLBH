package com.gener.qlbh.database;

import com.gener.qlbh.context.TenantContext;
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
                                   OrderRepository orderRepository, OrderService orderService, PurchaseOrderService purchaseOrderService,
                                   CompanyRepository companyRepository){
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                Company company = companyRepository.findByName("Default Co.")
                        .orElseGet(() -> companyRepository.save(
                                Company.builder().name("Default Co.").active(true).build()
                        ));

                try {
                    // Gắn DN hiện hành cho toàn bộ đoạn seed dưới đây
                    TenantContext.set(company);

                    // 1) Category (đã OK)
                    Category category = Category.builder()
                            .name("Tôn")
                            .method(Method.SHEET_METAL)
                            .defaultBaseUnit("m")
                            .build();
                    category.setCompany(company);     // BẮT BUỘC nếu @PrePersist không xử lý
                    categoryRepository.save(category);

                    // 2) Product qua service (service phải set product.setCompany(TenantContext.required()))
                    ProductCreateReq productCreateReq = ProductCreateReq.builder()
                            .name("Tôn Lạnh 0.40mm")
                            .retailPrice(160000.0)
                            .storePrice(155000.0)
                            .baseUnit("m")
                            .status(true)
                            .categoryId(category.getId())
                            .build();

                    ResponseEntity<ResponseObject> res = productService.createProduct(productCreateReq);

                    // Tuỳ cấu trúc ResponseObject của bạn:
                    // Nếu data trả ra Product:
                    Product createdProduct = (Product) res.getBody().getData();
                    String productId = createdProduct.getId();
                    String nameProduct = createdProduct.getName();

                    // 3) Customer tạo trực tiếp ⇒ NHỚ set company
                    Customer customer = Customer.builder()
                            .name("Test")
                            .phone("0586152003")
                            .address("HCM")
                            .build();
                    customer.setCompany(company);     // <<< THÊM DÒNG NÀY
                    customerRepository.save(customer);

                    // 4) Order qua service (service phải set order.setCompany(TenantContext.required()))
                    OrderDetailReq orderDetailReqA = OrderDetailReq.builder()
                            .productId(productId)
                            .length(2d).quantity(5d).price(160000d)
                            .name(nameProduct)
                            .build();
                    OrderDetailReq orderDetailReqB = OrderDetailReq.builder()
                            .quantity(1d).price(20000d)
                            .name("Công uốn")
                            .build();

                    OrderReq order = OrderReq.builder()
                            .note("Giao sáng mai, liên hệ trước 30 phút.")
                            .paidAmount(50000d)
                            .shippingFee(10000d)
                            .orderDetailReqs(Arrays.asList(orderDetailReqA, orderDetailReqB))
                            .customerId(customer.getId())
                            .build();
                    orderService.createOrder(order);

                    // 5) PurchaseOrder qua service (service phải set purchaseOrder.setCompany(TenantContext.required()))
                    PurchaseOrderCreateReq poReq = PurchaseOrderCreateReq.builder()
                            .productId(productId)
                            .totalLength(500000d)
                            .costPerUnit(80000d)
                            .supplier("Dong A")
                            .build();
                    purchaseOrderService.createPurchaseOrder(poReq);

                } finally {
                    TenantContext.clear(); // luôn dọn ThreadLocal
                }
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
