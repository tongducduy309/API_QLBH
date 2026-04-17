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
    @Transactional(rollbackFor  = Exception.class)
    public ResponseEntity<ResponseObject> createVariant(ProductVariantCreateReq req) throws APIException {
        if (req == null || req.getProductId() == null) {
            throw new APIException(ErrorCode.PRODUCT_ID_REQUIRED);
        }


        Product product = productRepository.findById(req.getProductId()).orElseThrow(
                ()-> new APIException(ErrorCode.PRODUCT_NOT_FOUND));

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

    @Transactional(rollbackFor  = Exception.class)
    public ResponseEntity<ResponseObject> updateVariant(ProductVariantUpdateReq req) throws APIException {
        ProductVariant existsProduct = variantRepository.findById(req.getId()).orElseThrow(
                ()-> new APIException(ErrorCode.VARIANT_NOT_FOUND));


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
