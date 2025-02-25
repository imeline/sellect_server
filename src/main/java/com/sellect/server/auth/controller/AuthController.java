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

        // todo: 환경변수
        ResponseCookie cookie = ResponseCookie.from("access_token", accessToken)
//            .domain("sellect.site") // ✅ 서브도메인에서도 유지하려면 설정
            .domain("sellect-client.vercel.app") // ✅ 서브도메인에서도 유지하려면 설정
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("None") // 반드시 추가
            .maxAge(Duration.ofMinutes(60))
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.ok(new LoginResponse(loginResponse.role(), loginResponse.nickname()));
    }

}
