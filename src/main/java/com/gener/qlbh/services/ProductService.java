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
import java.util.stream.Collectors;

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
        Product product = productRepository.findById(id).orElseThrow(
                ()-> new APIException(ErrorCode.PRODUCT_NOT_FOUND));
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
//        Category category = categoryRepository.findById(req.getCategoryId()).orElseThrow(()-> APIException.builder()
//                .status(ErrorCode.NOT_FOUND.getStatus())
//                .message("Cannot Found Category With Id = "+req.getCategoryId())
//                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
//                .build());


//        Inventory inventory = Inventory.builder()
//                .totalBaseUnitQty(0.0)
//                .build();

        Product product = productMapper.toProduct(req);

        if (product.getVariants()!=null){
            for (ProductVariant productVariant: product.getVariants()){
                productVariant.setProduct(product);
            }
        }


//        product.setCategory(category);
        product.setCategoryName(req.getCategoryName());
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
    public ResponseEntity<ResponseObject> deleteProduct(Long id) throws APIException {
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

        Product product = productRepository.findById(id).orElseThrow(
                ()-> new APIException(ErrorCode.PRODUCT_NOT_FOUND));

//        Category category = categoryRepository.findById(req.getCategoryId())
//                .orElseThrow(() -> APIException.builder()
//                        .status(ErrorCode.NOT_FOUND.getStatus())
//                        .message("Cannot Found Category With Id = "+req.getCategoryId())
//                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
//                        .build());

        product.setName(req.getName());
        product.setActive(req.isActive());
        product.setBaseUnit(req.getBaseUnit());
        product.setWarningQuantity(req.getWarningQuantity());
        product.setCategoryName(req.getCategoryName());
        product.setDescription(req.getDescription());
//        product.setCategory(category);

        // Map variant hiện có theo id
        Map<Long, ProductVariant> current = product.getVariants().stream()
                .filter(v -> v.getId() != null)
                .collect(Collectors.toMap(ProductVariant::getId, v -> v));

        Set<Long> keepIds = new HashSet<>();

        // update/create variant
        if (req.getVariants() != null) {
            for (ProductVariantUpdateReq vreq : req.getVariants()) {
                if (vreq.getId() == null) {
                    ProductVariant v = new ProductVariant();
                    v.setVariantCode(vreq.getVariantCode());
                    v.setProduct(product);
                    v.setSku(vreq.getSku());
                    v.setWeight(vreq.getWeight());
                    v.setRetailPrice(vreq.getRetailPrice());
                    v.setStorePrice(vreq.getStorePrice());
                    v.setActive(vreq.getActive());
                    product.getVariants().add(v);
                } else {
                    ProductVariant v = current.get(vreq.getId());
                    if (v == null) throw new APIException(ErrorCode.VARIANT_NOT_FOUND);
                    v.setVariantCode(vreq.getVariantCode());
                    v.setWeight(vreq.getWeight());
                    v.setSku(vreq.getSku());
                    v.setRetailPrice(vreq.getRetailPrice());
                    v.setStorePrice(vreq.getStorePrice());
                    v.setActive(vreq.getActive());

                    keepIds.add(v.getId());
                }
            }
        }

        // tìm variant cần xóa: lấy từ DB (product.getVariants) so với keepIds
        List<Long> deleteIds = product.getVariants().stream()
                .map(ProductVariant::getId)
                .filter(Objects::nonNull)
                .filter(vid -> !keepIds.contains(vid))
                .toList();

        if (!deleteIds.isEmpty() && orderDetailRepository.existsByProductVariant_IdIn(deleteIds)) {
            throw new APIException(ErrorCode.VARIANT_IN_USE);
        }

        // remove khỏi collection => orphanRemoval ở Product-Variant sẽ delete variant
        product.getVariants().removeIf(v -> v.getId() != null && deleteIds.contains(v.getId()));

        Product saved = productRepository.save(product);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode())
                .body(new ResponseObject(SuccessCode.REQUEST.getStatus(), "Update Product Successfully", saved));
    }

    @Transactional
    public ResponseEntity<ResponseObject> updateWishlish(Long id, ProductWishlistUpdateReq req) throws APIException {
        Product product = productRepository.findById(id).orElseThrow(
                ()-> new APIException(ErrorCode.PRODUCT_NOT_FOUND));
//        product.setWishlist(req.isWishlist());
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(SuccessCode.REQUEST.getStatus(), "Update Wishlist Of Product Successfully",productRepository.save(product))
        );
    }
}
