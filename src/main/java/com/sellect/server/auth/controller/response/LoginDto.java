package com.sellect.server.auth.controller.response;

public record LoginDto(
    String accessToken,
    String role,
    String nickname
) {

}
