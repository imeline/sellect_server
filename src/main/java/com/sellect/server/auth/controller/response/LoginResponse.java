package com.sellect.server.auth.controller.response;

public record LoginResponse(
    String role,
    String nickname
) {

}
