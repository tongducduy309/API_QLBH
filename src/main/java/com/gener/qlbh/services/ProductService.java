package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.ProductCreateReq;
import com.gener.qlbh.dtos.request.ProductUpdateReq;
import com.gener.qlbh.dtos.request.ProductVariantUpdateReq;
import com.gener.qlbh.dtos.request.ProductWishlistUpdateReq;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.mapper.ProductMapper;
import com.gener.qlbh.models.*;
import com.gener.qlbh.repositories.CategoryRepository;
import com.gener.qlbh.repositories.OrderDetailRepository;
import com.gener.qlbh.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final OrderDetailRepository orderDetailRepository;

    @Transactional
    public ResponseEntity<ResponseObject> getAllProducts(){
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get All Product Successfully")
                        .data(productRepository.findAll())
                        .build()
        );

    }

    @Transactional
    public ResponseEntity<ResponseObject> getProductById(Long id) throws APIException {
        Product product = productRepository.findById(id).orElseThrow(()-> APIException.builder()
                .status(ErrorCode.NOT_FOUND.getStatus())
                .message("Cannot Found Product With Id = "+id)
                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                .build());
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get Product With Id = "+id+" Successfully")
                        .data(product)
                        .build()
        );

    }

    @Transactional
    public ResponseEntity<ResponseObject> createProduct(ProductCreateReq req) throws APIException {
        Category category = categoryRepository.findById(req.getCategoryId()).orElseThrow(()-> APIException.builder()
                .status(ErrorCode.NOT_FOUND.getStatus())
                .message("Cannot Found Category With Id = "+req.getCategoryId())
                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                .build());


//        Inventory inventory = Inventory.builder()
//                .totalBaseUnitQty(0.0)
//                .build();

        Product product = productMapper.toProduct(req);

        if (product.getVariants()!=null){
            for (ProductVariant productVariant: product.getVariants()){
                productVariant.setProduct(product);
            }
        }


        product.setCategory(category);
//        product.setInventory(inventory);
        return ResponseEntity.status(SuccessCode.CREATE.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.CREATE.getStatus())
                        .message("Create Product Successfully")
                        .data(productRepository.save(product))
                        .build()
        );

    }

    @Transactional
    public ResponseEntity<ResponseObject> deleteProduct(Long id){
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()){
            productRepository.deleteById(id);

        }
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(SuccessCode.REQUEST.getStatus(), "Delete Product Successfully","")
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> updateProduct(Long id, ProductUpdateReq req) throws APIException {
        boolean existsProduct = productRepository.existsById(id);
        if (!existsProduct) {
            throw APIException.builder()
                    .status(ErrorCode.NOT_FOUND.getStatus())
                    .message("Cannot Found Product With Id = " + id)
                    .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                    .build();
        }

        Category category = categoryRepository.findById(req.getCategoryId()).orElseThrow(()-> APIException.builder()
                .status(ErrorCode.NOT_FOUND.getStatus())
                .message("Cannot Found Category With Id = "+req.getCategoryId())
                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                .build());

        Product newProduct = productMapper.toProduct(req);
        newProduct.setId(id);

        newProduct.setCategory(category);
        Set<Long> keepIds = new HashSet<>();

        if (newProduct.getVariants()!=null){
            for (ProductVariant productVariant: newProduct.getVariants()){
                productVariant.setProduct(newProduct);
                keepIds.add(productVariant.getId());
            }
        }

        List<Long> deleteIds = req.getVariants().stream()
                .map(ProductVariantUpdateReq::getId)
                .filter(Objects::nonNull)
                .filter(vid -> !keepIds.contains(vid))
                .toList();

        if (!deleteIds.isEmpty() && orderDetailRepository.existsByProductVariant_IdIn(deleteIds)) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getStatus())
                    .message("Cannot delete product variants because they are used in order details: " + deleteIds)
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatusCode())
                    .build();
        }

        if (!deleteIds.isEmpty()) {
            req.getVariants().removeIf(v -> v.getId() != null && deleteIds.contains(v.getId()));
        }





        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(SuccessCode.REQUEST.getStatus(), "Update Product Successfully",productRepository.save(newProduct))
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> updateWishlish(Long id, ProductWishlistUpdateReq req) throws APIException {
        Product product = productRepository.findById(id).orElseThrow(()-> APIException.builder()
                .status(ErrorCode.NOT_FOUND.getStatus())
                .message("Cannot Found Product With Id = "+id)
                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                .build());
        product.setWishlist(req.isWishlist());
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(SuccessCode.REQUEST.getStatus(), "Update Wishlist Of Product Successfully",productRepository.save(product))
        );
    }
}
