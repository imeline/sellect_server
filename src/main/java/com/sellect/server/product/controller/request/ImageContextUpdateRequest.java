package com.sellect.server.product.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ImageContextUpdateRequest(
    Long productImageId,

    @NotNull(message = "이미지 순서는 필수 입력값입니다.")
    Integer sequence,

    String uuid,

    @NotBlank(message = "파일명은 필수 입력값입니다.")
    String filename,

    boolean isRepresentative,
    boolean isNewImage
) {

}
