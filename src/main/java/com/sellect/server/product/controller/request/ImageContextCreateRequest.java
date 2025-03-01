package com.sellect.server.product.controller.request;

import lombok.Builder;

@Builder
public record ImageContextCreateRequest(
    Integer sequence,
    String uuid,
    String filename,
    boolean isRepresentative
) {

}
