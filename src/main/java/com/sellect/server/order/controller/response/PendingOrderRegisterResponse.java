package com.sellect.server.order.controller.response;

import lombok.Builder;

@Builder
public record PendingOrderRegisterResponse(
    Long orderId
) {

}
