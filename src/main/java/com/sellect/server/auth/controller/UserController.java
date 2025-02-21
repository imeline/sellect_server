package com.sellect.server.auth.controller;


import com.sellect.server.auth.application.UserService;
import com.sellect.server.auth.controller.response.UserInfoResponse;
import com.sellect.server.auth.domain.User;
import com.sellect.server.common.infrastructure.annotation.AuthUser;
import com.sellect.server.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public ApiResponse<UserInfoResponse> getUserInfo(@AuthUser User user) {
        UserInfoResponse userInfo = userService.getUserInfo(user);
        return ApiResponse.ok(userInfo);
    }

    @DeleteMapping("/leave")
    public ApiResponse<Void> leave(@AuthUser User user) {
        userService.leave(user);
        return ApiResponse.ok(null);
    }


}
