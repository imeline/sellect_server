package com.sellect.server.brand.controller;

import com.sellect.server.brand.application.BrandService;
import com.sellect.server.brand.controller.response.BrandRetrieveResponse;
import com.sellect.server.common.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping("/brands")
    public ApiResponse<List<BrandRetrieveResponse>> retrieveBrandsByCategory(@RequestParam("brand_name") String brandName) {
        List<BrandRetrieveResponse> results = brandService.retrieveBrandsContainingName(brandName);
        return ApiResponse.ok(results);
    }
}
