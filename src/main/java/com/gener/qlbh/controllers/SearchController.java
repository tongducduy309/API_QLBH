package com.gener.qlbh.controllers;

import com.gener.qlbh.services.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<?> globalSearch(
            @RequestParam("q") String keyword,
            @RequestParam(value = "limit", defaultValue = "8") int limit
    ) {
        return ResponseEntity.ok(searchService.globalSearch(keyword, limit));
    }
}