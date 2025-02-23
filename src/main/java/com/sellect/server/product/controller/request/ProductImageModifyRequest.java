package com.sellect.server.product.controller.request;

import java.util.List;
import lombok.Builder;

@Builder
public record ProductImageModifyRequest(
    Long productId,
    List<Long> productImageIdsToDelete,
    List<ImageContextUpdateRequest> productImagesToUpdate
) {

}
