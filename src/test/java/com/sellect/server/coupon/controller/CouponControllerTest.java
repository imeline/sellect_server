//package com.sellect.server.coupon.controller;
//
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doAnswer;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.PropertyNamingStrategies;
//import com.sellect.server.auth.domain.User;
//import com.sellect.server.common.infrastructure.jwt.JwtFilter;
//import com.sellect.server.common.resolver.AuthenticationResolver;
//import com.sellect.server.config.SecurityConfig;
//import com.sellect.server.coupon.application.CouponService;
//import com.sellect.server.coupon.controller.request.IssueCouponRequest;
//import com.sellect.server.coupon.controller.response.ActiveCouponResponse;
//import com.sellect.server.coupon.controller.response.CouponInfo;
//import com.sellect.server.coupon.controller.response.CouponPossibleOrderResponse;
//import com.sellect.server.coupon.controller.response.CouponResponse;
//import com.sellect.server.coupon.controller.response.SellerInfo;
//import jakarta.servlet.FilterChain;
//import java.time.LocalDate;
//import java.util.Arrays;
//import java.util.List;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
//import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
//import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.MediaType;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.test.web.servlet.MockMvc;
//
//@WebMvcTest(CouponController.class)
//@ImportAutoConfiguration(
//    exclude = {
//        SecurityAutoConfiguration.class,
//        ManagementWebSecurityAutoConfiguration.class,
//        SecurityConfig.class,
//        SecurityFilterChain.class
//    })
//class CouponControllerTest {
//
//    @MockBean
//    JwtFilter jwtFilter;
//
//    @MockBean
//    SecurityFilterChain securityFilterChain;
//
//    @MockBean
//    AuthenticationResolver authenticationResolver;
//
//    @MockBean
//    org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private CouponService couponService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private User mockUser;
//
//    @BeforeEach
//    void setUp() throws Exception {
//        mockUser = User.builder().id(1L).nickname("testUser").build(); // User 필드에 따라 조정
//
//        doNothing().when(jwtFilter).doFilter(any(), any(), any()); // 필터 통과
//        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
//    }
//
//    @Test
//    @DisplayName("쿠폰 발급 테스트")
//    void testIssueCoupon() throws Exception {
//        // Given
//        IssueCouponRequest request = new IssueCouponRequest(10, 1000,
//            LocalDate.now().plusDays(3)); // 필드 설정 필요
//
//
//        doAnswer(invocation -> {
//            invocation.getArgument(2, FilterChain.class).doFilter(
//                invocation.getArgument(0), invocation.getArgument(1));
//            return null;
//        }).when(jwtFilter).doFilter(any(), any(), any());
//
//        when(authenticationResolver.resolveArgument(any(), any(), any(), any())).thenReturn(mockUser); // Resolver 모킹
//        doNothing().when(couponService).uploadCoupon(any(User.class), any(IssueCouponRequest.class));
//
//        System.out.println(objectMapper.writeValueAsString(request));
//
//        // When & Then
//        mockMvc.perform(post("/api/v1/coupon/issue")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//            .andExpect(status().isOk())
//            .andDo(print());
//
//        verify(authenticationResolver).resolveArgument(any(), any(), any(), any());
//        verify(couponService).uploadCoupon(any(User.class), any(IssueCouponRequest.class));
////            .andExpect(jsonPath("$.is_success").value(true))
////            .andExpect(jsonPath("$.status").value(200))
////            .andExpect(jsonPath("$.code").value(""))
////            .andExpect(jsonPath("$.message").value(""));
//    }
//
//    @Test
//    void testRegisterCoupon() throws Exception {
//        // Given
//        Long couponId = 1L;
//        doNothing().when(couponService).downloadCoupon(any(User.class), any(Long.class));
//
//        // When & Then
//        mockMvc.perform(put("/api/v1/coupon/register/{couponId}", couponId))
//            .andExpect(status().isOk())
//            .andDo(print());
////            .andExpect(jsonPath("$.success").value(true));
//    }
//
//    @Test
//    void testGetCouponList() throws Exception {
//        // Given
//        List<CouponResponse> couponList = Arrays.asList(
//            new CouponResponse(null,
//                new CouponInfo(1L, 100, LocalDate.now().plusDays(10), new SellerInfo(2L, "seller")))
//            // 필드 설정 필요
//        );
//        when(couponService.getCouponList(mockUser, 0, 5, null))
//            .thenReturn(couponList);
//
//        // When & Then
//        mockMvc.perform(get("/api/v1/coupon")
//                .param("page", "0")
//                .param("size", "5"))
//            .andExpect(status().isOk())
//            .andDo(print());
////            .andExpect(jsonPath("$.success").value(true))
////            .andExpect(jsonPath("$.data").isArray())
////            .andExpect(jsonPath("$.data").value(couponList));
//    }
//
//    @Test
//    void testGetActiveCouponList() throws Exception {
//        // Given
//        Pageable pageable = PageRequest.of(0, 5);
//        List<ActiveCouponResponse> activeCoupons = Arrays.asList(
//            new ActiveCouponResponse(true,
//                new CouponInfo(1L, 100, LocalDate.now().plusDays(10), new SellerInfo(2L, "seller")))
//            // 필드 설정 필요
//        );
//        Page<ActiveCouponResponse> pageResult = new PageImpl<>(activeCoupons, pageable,
//            activeCoupons.size());
//        when(couponService.getActiveCouponList(mockUser, pageable))
//            .thenReturn(pageResult);
//
//        // When & Then
//        mockMvc.perform(get("/api/v1/coupon/actives"))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.success").value(true))
//            .andExpect(jsonPath("$.data.content").isArray())
//            .andExpect(jsonPath("$.data.pageable.pageSize").value(5))
//            .andExpect(jsonPath("$.data.pageable.pageNumber").value(0));
//    }
//
//    @Test
//    void testGetPossibleOrderCouponList() throws Exception {
//        // Given
//        List<Long> productIds = Arrays.asList(1L, 2L);
//        List<CouponPossibleOrderResponse> couponList = Arrays.asList(
//            new CouponPossibleOrderResponse(1L, 1000, LocalDate.now().plusDays(5)) // 필드 설정 필요
//        );
//        when(couponService.getCouponsByMatchingSeller(mockUser, productIds))
//            .thenReturn(couponList);
//
//        // When & Then
//        mockMvc.perform(get("/api/v1/coupon/possible-order")
//                .param("productIds", "1", "2"))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.success").value(true))
//            .andExpect(jsonPath("$.data").isArray())
//            .andExpect(jsonPath("$.data").value(couponList));
//    }
//}