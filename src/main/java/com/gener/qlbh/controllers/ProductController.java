package com.gener.qlbh.controllers;

import com.gener.qlbh.dtos.request.ProductCreateReq;
import com.gener.qlbh.dtos.request.ProductUpdateReq;
import com.gener.qlbh.dtos.request.ProductWishlistUpdateReq;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.models.Product;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.services.ProductService;
import com.gener.qlbh.services.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    ResponseEntity<ResponseObject> getAllProducts(){
        return productService.getAllProducts();
    }


    @PostMapping
    ResponseEntity<ResponseObject> createProduct(@RequestBody ProductCreateReq req) throws APIException {
        return productService.createProduct(req);
    }

    @GetMapping("/{id}")
    ResponseEntity<ResponseObject> getProductById(@PathVariable Long id) throws APIException{
        return productService.getProductById(id);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ResponseObject> deleteProduct(@PathVariable Long id){
        return productService.deleteProduct(id);
    }

    @PutMapping("/{id}")
    ResponseEntity<ResponseObject> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateReq req) throws APIException {
        return productService.updateProduct(id,req);
    }

    @PutMapping("wishlist/{id}")
    ResponseEntity<ResponseObject> updateWishlistProduct(@PathVariable Long id, @RequestBody ProductWishlistUpdateReq req) throws APIException {
        return productService.updateWishlish(id,req);
    }
}
