package com.gener.qlbh.database;

import com.gener.qlbh.dtos.request.*;
import com.gener.qlbh.dtos.request.OrderDetailCreateReq;
import com.gener.qlbh.enums.OrderDetailKind;
import com.gener.qlbh.enums.OrderStatus;
import com.gener.qlbh.models.*;
import com.gener.qlbh.repositories.*;
import com.gener.qlbh.services.OrderService;
import com.gener.qlbh.services.ProductService;
import com.gener.qlbh.services.ProductVariantService;
import com.gener.qlbh.services.PurchaseReceiptsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;


@Configuration
@Slf4j
public class database {
    @Bean
    CommandLineRunner initDatabase(CategoryRepository categoryRepository, ProductRepository productRepository, ProductService productService, CustomerRepository customerRepository,
                                   OrderRepository orderRepository, OrderService orderService, PurchaseReceiptsService purchaseReceiptsService, ProductVariantService productVariantService
                                   ){
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {

//                Category category = Category.builder()
//                        .name("Tôn")
//                        .defaultBaseUnit("m")
//                        .build();
//                categoryRepository.save(category);
                ProductCreateReq productCreateReq = ProductCreateReq.builder()
                        .name("Tôn Lạnh")
                        .baseUnit("m")
                        .active(true)
//                        .categoryId(category.getId())
                        .description("Tôn lạnh (hay còn gọi là tôn mạ nhôm kẽm) là một loại vật liệu kim loại được sử dụng rất phổ biến trong xây dựng và công nghiệp nhờ vào khả năng chống ăn mòn và phản xạ nhiệt vượt trội. ")
                        .categoryName("Tôn")
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

// sau đó tạo purchase order cho variant
                PurchaseReceiptsCreateReq purchaseReceiptsCreateReq = PurchaseReceiptsCreateReq.builder()
                        .productVariantId(variantId)  // dùng variant id
                        .totalQuantity(500d)
                        .cost(80000d)
                        .totalCost(40000000d)
                        .supplier("Dong A")
                        .build();
                purchaseReceiptsService.createPurchaseReceipts(purchaseReceiptsCreateReq);

                OrderDetailCreateReq orderDetailCreateReqA = OrderDetailCreateReq.builder()
//                        .productVariantId()(productId)
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
//                Product product = Product.builder()
//                        .name("Tôn Lạnh 0.40mm")
////                        .retailPrice(160000.0)
////                        .storePrice(155000.0)
//                        .baseUnit("m")
////                        .category(category)
//                        .build();
//                productRepository.save(product);


            }
        };
    }
}
