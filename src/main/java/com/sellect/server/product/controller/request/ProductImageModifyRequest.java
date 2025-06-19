package com.sellect.server.product.controller.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

@Builder
public record ProductImageModifyRequest(

    @NotNull(message = "상품 ID는 필수 입력값입니다.")
    Long productId,
    @NotNull(message = "삭제할 이미지 ID 리스트는 필수 입력값입니다. (삭제할 이미지가 없다면 빈 리스트를 전달해주세요.)")
    List<Long> productImageIdsToDelete,
    @NotNull(message = "이미지 정보 업데이트 요청 리스트는 필수 입력값입니다. (업데이트할 이미지가 없다면 빈 리스트를 전달해주세요.)")
    List<ImageContextUpdateRequest> productImagesToUpdate
) {

}
