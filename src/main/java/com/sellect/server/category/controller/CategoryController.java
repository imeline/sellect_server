package com.sellect.server.category.controller;

import com.sellect.server.category.application.CategoryService;
import com.sellect.server.category.controller.request.CategoryReadResponse;
import com.sellect.server.common.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public ApiResponse<List<CategoryReadResponse>> readCategories() {
        List<CategoryReadResponse> result = categoryService.readCategories();
        return ApiResponse.ok(result);
    }
}