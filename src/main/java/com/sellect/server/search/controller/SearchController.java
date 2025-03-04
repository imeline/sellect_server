package com.sellect.server.search.controller;

import com.sellect.server.auth.domain.User;
import com.sellect.server.common.infrastructure.annotation.AuthUser;
import com.sellect.server.common.response.ApiResponse;
import com.sellect.server.search.application.AutoCompleteService;
import com.sellect.server.search.application.SearchService;
import com.sellect.server.search.controller.response.AutoCompleteResponse;
import com.sellect.server.search.controller.response.SearchResponse;
import com.sellect.server.search.domain.SearchCondition;
import com.sellect.server.search.domain.SearchSortType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final AutoCompleteService autoCompleteService;
    private final SearchService searchService;

    @GetMapping("/auto-complete")
    public ApiResponse<AutoCompleteResponse> autoCompleteSearch(
        @RequestParam(value = "query") String query) {
        return ApiResponse.ok(
            new AutoCompleteResponse(autoCompleteService.getAutoCompleteResult(query)));
    }

    /*
     * 상품 조회 API
     * condition 을 통해 확인
     * */
    @GetMapping("/products")
    public ApiResponse<Page<SearchResponse>> searchTotal(
        @AuthUser User user,
        @RequestParam String keyword,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Long brandId,
        @RequestParam(required = false) Integer minPrice,
        @RequestParam(required = false) Integer maxPrice,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "LATEST") SearchSortType sortType,
        HttpServletRequest request
    ) {
        // 1.세션 가져오기 (없으면 생성)
        String sessionId = request.getSession(true).getId();

        // 2. 회원 여부 판단 (userId가 존재하면 USER, 아니면 GUEST)
        boolean isUser = (user != null);
        String userIdentifier = (isUser ? "USER_" : "GUEST_") + sessionId;

        // 3. USER / GUEST 식별자 생성
        SearchCondition condition = SearchCondition
            .create(keyword, categoryId, brandId, minPrice, maxPrice);

        Page<SearchResponse> results = searchService.searchTotal(userIdentifier, condition,
            page, size, sortType);

        return ApiResponse.ok(results);
    }
}
