package com.sellect.server.auth.controller;

import com.sellect.server.auth.application.UserAuthService;
import com.sellect.server.auth.controller.request.LoginRequest;
import com.sellect.server.auth.controller.request.UserSignUpRequest;
import com.sellect.server.auth.controller.response.LoginDto;
import com.sellect.server.auth.controller.response.LoginResponse;
import com.sellect.server.auth.repository.entity.Role;
import com.sellect.server.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserAuthService userAuthService;

    @PostMapping("/signup")
    public ApiResponse<Void> signup(@RequestBody @Valid UserSignUpRequest request) {
        userAuthService.signUp(request, Role.USER);
        return ApiResponse.ok(null);
    }

    @PostMapping("/seller/signup")
    public ApiResponse<Void> sellerSignUp(@RequestBody UserSignUpRequest request) {
        userAuthService.signUp(request, Role.SELLER);
        return ApiResponse.ok(null);
    }


    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request,
        HttpServletResponse response) {
        LoginDto loginResponse = userAuthService.login(request);
        String accessToken = loginResponse.accessToken();

        ResponseCookie cookie = ResponseCookie.from("access_token", accessToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("None") // ✅ SameSite=None 설정
            .maxAge(Duration.ofMinutes(60))
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.ok(new LoginResponse(loginResponse.role(), loginResponse.nickname()));
    }

}
