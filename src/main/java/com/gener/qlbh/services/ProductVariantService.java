package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.ProductVariantCreateReq;
import com.gener.qlbh.dtos.request.ProductVariantUpdateReq;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.mapper.ProductMapper;
import com.gener.qlbh.models.Product;
import com.gener.qlbh.models.ProductVariant;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.repositories.ProductRepository;
import com.gener.qlbh.repositories.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ResponseEntity<ResponseObject> getAllVariants(){
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get All Product Variants Successfully")
                        .data(productMapper.toProductVariantRes(variantRepository.findAll()))
                        .build()
        );
    }
    @Transactional
    public ResponseEntity<ResponseObject> createVariant(ProductVariantCreateReq req) throws APIException {
        if (req == null || req.getProductId() == null) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getStatus())
                    .message("ProductId is required")
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatusCode())
                    .build();
        }


        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> APIException.builder()
                        .status(ErrorCode.NOT_FOUND.getStatus())
                        .message("Cannot Found Product With Id = " + req.getProductId())
                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                        .build());

        ProductVariant variant = ProductVariant.builder()
                .variantCode(req.getVariantCode())
                .weight(req.getWeight())
                .retailPrice(req.getRetailPrice())
                .sku(req.getSku())
                .storePrice(req.getStorePrice())
                .active(req.getActive() == null || req.getActive())
                .build();

        variant.setProduct(product);
        try {
            product.getVariants().add(variant);
        } catch (Exception ignored) {}

        variantRepository.save(variant);

        return ResponseEntity.status(SuccessCode.CREATE.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.CREATE.getStatus())
                        .message("Create ProductVariant Successfully")
                        .data(variant)
                        .build()
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> updateVariant(ProductVariantUpdateReq req) throws APIException {
        ProductVariant existsProduct = variantRepository.findById(req.getId()).orElseThrow(()-> APIException.builder()
                .status(ErrorCode.NOT_FOUND.getStatus())
                .message("Cannot Found Product Variant With Id = " + req.getId())
                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                .build());


        ProductVariant variant = productMapper.toProductVariant(req);
        variant.setProduct(existsProduct.getProduct());

        variantRepository.save(variant);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Update Product Variant Successfully")
                        .data(variant)
                        .build()
        );
    }



}
