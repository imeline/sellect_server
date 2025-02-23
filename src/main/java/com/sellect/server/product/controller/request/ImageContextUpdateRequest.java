package com.sellect.server.product.controller.request;

import lombok.Builder;

@Builder
public record ImageContextUpdateRequest(
    Long productImageId,
    Integer sequence,
    String uuid,
    boolean isRepresentative,
    boolean isNewImage
) {

}
