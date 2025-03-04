package com.sellect.server.auth.controller.response;

public record LoginResponse(
    String accessToken,
    String role,
    String nickname
) {

}
