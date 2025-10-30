package com.gener.qlbh.services;

import com.gener.qlbh.context.TenantContext;
import com.gener.qlbh.dtos.request.ProductCreateReq;
import com.gener.qlbh.dtos.request.ProductUpdateReq;
import com.gener.qlbh.dtos.request.ProductWishlistUpdateReq;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.mapper.ProductMapper;
import com.gener.qlbh.models.*;
import com.gener.qlbh.repositories.CategoryRepository;
import com.gener.qlbh.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

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
    public ResponseEntity<ResponseObject> getProductById(String id) throws APIException {
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

        Company curr = TenantContext.required();
        if (!category.getCompany().getId().equals(curr.getId())) {
            throw new IllegalStateException("Category không thuộc company hiện hành");
        }

        Inventory inventory = Inventory.builder()
                .totalBaseUnitQty(0.0)
                .build();

        Product product = productMapper.toProduct(req);

        product.setCategory(category);
        product.setInventory(inventory);
        product.setCompany(curr);
        return ResponseEntity.status(SuccessCode.CREATE.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.CREATE.getStatus())
                        .message("Create Product Successfully")
                        .data(productRepository.save(product))
                        .build()
        );

    }

    @Transactional
    public ResponseEntity<ResponseObject> deleteProduct(String id){
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()){
            productRepository.deleteById(id);

        }
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(SuccessCode.REQUEST.getStatus(), "Delete Product Successfully","")
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> updateProduct(String id, ProductUpdateReq req) throws APIException {
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


        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(SuccessCode.REQUEST.getStatus(), "Update Product Successfully",productRepository.save(newProduct))
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> updateWishlish(String id, ProductWishlistUpdateReq req) throws APIException {
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
