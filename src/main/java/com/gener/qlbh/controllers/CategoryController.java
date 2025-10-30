package com.gener.qlbh.controllers;

import com.gener.qlbh.models.Category;
import com.gener.qlbh.models.Product;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.services.CategoryService;
import com.gener.qlbh.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    ResponseEntity<ResponseObject> getAllCategories(){
        return categoryService.getAllCategories();
    }

    @PostMapping
    ResponseEntity<ResponseObject> createCategory(@RequestBody Category req){
        return categoryService.createCategory(req);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ResponseObject> deleteCategory(@PathVariable Long id){
        return categoryService.deleteCategory(id);
    }
}
