package com.sellect.server.coupon.controller.response;

public record SellerInfo(
    Long sellerId,
    String sellerNickname
) {
    public static SellerInfo from(Long sellerId, String sellerNickname) {
        return new SellerInfo(sellerId, sellerNickname);
    }
}
