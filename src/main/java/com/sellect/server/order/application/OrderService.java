package com.sellect.server.order.application;

import com.sellect.server.auth.domain.User;
import com.sellect.server.auth.repository.user.UserRepository;
import com.sellect.server.cart.domain.CartItem;
import com.sellect.server.cart.repository.CartItemRepository;
import com.sellect.server.common.exception.CommonException;
import com.sellect.server.common.exception.enums.BError;
import com.sellect.server.coupon.domain.Coupon;
import com.sellect.server.coupon.domain.UserReceivedCoupon;
import com.sellect.server.coupon.repository.UserReceivedCouponRepository;
import com.sellect.server.order.Infrastructure.port.KakaoPayClient;
import com.sellect.server.order.Infrastructure.response.KakaoPayApproveResponse;
import com.sellect.server.order.Infrastructure.response.KakaoPayReadyResponse;
import com.sellect.server.order.controller.request.OrderAddRequest;
import com.sellect.server.order.controller.response.OrderDetailGetResponse;
import com.sellect.server.order.controller.response.OrderGetResponse;
import com.sellect.server.order.controller.response.OrderItemGetResponse;
import com.sellect.server.order.domain.OrderItem;
import com.sellect.server.order.domain.Orders;
import com.sellect.server.order.repository.OrderItemRepository;
import com.sellect.server.order.repository.OrdersRepository;
import com.sellect.server.order.repository.entity.OrderStatus;
import com.sellect.server.order.Infrastructure.request.KakaoPayReadyRequest;
import com.sellect.server.payment.controller.request.ApproveRequest;
import com.sellect.server.payment.domain.Payment;
import com.sellect.server.payment.repository.PaymentRepository;
import com.sellect.server.product.domain.Product;
import com.sellect.server.product.repository.ProductImageRepository;
import com.sellect.server.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartRepository;
    private final UserReceivedCouponRepository userReceivedCouponRepository;
    private final ProductImageRepository productImageRepository;
    private final KakaoPayClient kakaoPayClient;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public List<OrderItemGetResponse> readPending(User user, Long orderId) {
        // 주문이 실제로 존재하는지 체크
        Orders order = ordersRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("존재하지 않는 주문입니다."));
        // 유저의 주문이 맞는지 체크
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("해당 주문에 접근 권한이 없습니다.");
        }
        // 결제 대기 주문인지 체크
        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new RuntimeException("결제 대기 주문이 아닙니다.");
        }

        List<OrderItem> orders = orderItemRepository.findAllByOrdersId(orderId);
        // 주문에 상품이 하나도 없는 경우라면 조회 x
        if (orders.isEmpty()) {
            throw new RuntimeException("올바르지 않은 orderId 입니다.");
        }

        return orders.stream()
            .map(this::convertToOrderItemResponse)
            .toList();
    }

    @Transactional
    public Orders registerPendingOrder(User user, OrderAddRequest request) {
        // 재고 확인
        request.orderItems().forEach(item -> {
            Product product = productRepository.findById(item.productId())
                .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "product"));
            if (product.getStock() < item.quantity()) {
                throw new CommonException(BError.NOT_VALID, "재고 부족");
            }
        });
        // Orders 저장
        Orders order = Orders.register(user, request.convertPriceAsBigDecimal(),
            OrderStatus.PENDING);
        Orders savedOrder = ordersRepository.save(order);

        // productId 중복 확인을 위해 set
        Set<Long> productIds = new HashSet<>();

        // OrderAddRequest -> OrderItemAddRequest -> OrderItem 으로 변환
        List<OrderItem> orderItems = request.orderItems().stream()
            .map(orderItemAddRequest -> {
                if (!productIds.add(orderItemAddRequest.productId())) {
                    throw new CommonException(BError.EXIST, "productId");
                }

                Product product = productRepository.findById(orderItemAddRequest.productId())
                    .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "product"));

                return OrderItem.register(
                    savedOrder,
                    product,
                    orderItemAddRequest.convertPriceAsBigDecimal(),
                    orderItemAddRequest.quantity()
                );
            })
            .toList();
        // OrderItem 저장
        orderItemRepository.saveAll(orderItems);

        return savedOrder;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Orders lockAndCompleteOrder(User user, Long orderId) {
        Orders order = getOrderById(orderId);
        // 이미 완료된 주문인지 확인
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new CommonException(BError.NOT_VALID, "이미 완료(확정)된 주문입니다.");
        }
        List<OrderItem> orderItems = getOrderItemsByOrderId(orderId);

        orderItems.forEach(orderItem -> {
            // DB 락
            productRepository.findByIdWithLock(orderItem.getProduct().getId())
                .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "상품이 존재하지 않습니다."));
            // 재고 확인
            if (orderItem.getProduct().getStock() < orderItem.getQuantity()) {
                throw new CommonException(BError.NOT_VALID, "재고 부족");
            }
            // 재고 차감
            productRepository.save(orderItem.getProduct().updateStock(orderItem.getQuantity()));
        });
        // 주문 확정
        Orders savedOrder = ordersRepository.save(order.updateStatus(OrderStatus.COMPLETED));

        // 쿠폰 사용 처리, 장바구니 비우기
        clearCartAndDeleteCouponAsync(user, savedOrder);

        return savedOrder;
    }

    // 비동기 처리
    @Async
    public void clearCartAndDeleteCouponAsync(User user, Orders order) {
        clearCartAndDeleteCoupon(user, order);
    }

    @Transactional
    public void clearCartAndDeleteCoupon(User user, Orders order) {
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new CommonException(BError.NOT_VALID, "주문이 완료되지 않았습니다.");
        }
        // 장바구니 비우기
        List<CartItem> cartItems = cartRepository.findAllByUserId(user.getId());
        List<CartItem> removedCartItems = cartItems.stream().map(CartItem::remove).toList();

        cartRepository.saveAll(removedCartItems);

        // 쿠폰 사용 처리
        if (order.getUserReceivedCoupon() != null) {
            userReceivedCouponRepository.save(order.getUserReceivedCoupon().useCoupon());
        }
    }

    public List<OrderGetResponse> getOrdersByUser(User user) {
        List<Orders> orderList = ordersRepository.findCompletedOrdersByUser(user,
            OrderStatus.COMPLETED);

        if (orderList.isEmpty()) {
            throw new CommonException(BError.NOT_EXIST, "주문 목록");
        }

        return orderList.stream()
            .map(order -> {
                List<OrderItemGetResponse> orderItems = orderItemRepository.findAllByOrdersId(
                        order.getId())
                    .stream()
                    .map(this::convertToOrderItemResponse)
                    .toList();

                return OrderGetResponse.from(order, orderItems);
            })
            .toList();
    }


    public OrderDetailGetResponse getOrderDetail(Long orderId) {
        Orders order = getOrderById(orderId);

        if (order.getStatus() == OrderStatus.PENDING) {
            throw new CommonException(BError.NOT_VALID, "PENDING 상태의 주문은 조회할 수 없습니다.");
        }

        List<OrderItemGetResponse> orderItems = getOrderItemsByOrderId(orderId).stream()
            .map(this::convertToOrderItemResponse)
            .toList();

        BigDecimal discountCost = Optional.ofNullable(order.getUserReceivedCoupon())
            .map(UserReceivedCoupon::getCoupon)
            .map(Coupon::getDiscountCost)
            .map(BigDecimal::valueOf)
            .orElse(BigDecimal.ZERO);

        return OrderDetailGetResponse.from(order, discountCost, orderItems);
    }


    @Transactional
    public void applyCouponToOrder(User user, Long orderId, Long couponId) {
        Orders order = getOrderById(orderId);
        // 이미 완료된 주문인지 확인
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new CommonException(BError.NOT_VALID, "이미 완료(확정)된 주문입니다.");
        }
        // 쿠폰 조회
        UserReceivedCoupon coupon = getUserValidateReceivedCoupon(user,
            couponId);
        // 주문에 쿠폰 정보 저장
        ordersRepository.save(order.updateCoupon(coupon));
        // 쿠폰 사용 처리는 주문 확정 후 적용
        //userReceivedCouponRepository.save(coupon.useCoupon());
    }

    private OrderItemGetResponse convertToOrderItemResponse(OrderItem orderItem) {
        // todo: N+1 발생
        // todo: BrandRepository를 Response에서 brandName 가져올 때 JPA에서 조회를 통해 가져옴
        // todo: N+1 문제 발생 (일단 임시로 구현)
        Product product = productRepository.findById(orderItem.getProduct().getId())
            .orElseThrow(() -> new RuntimeException("유효하지 않은 상품 번호입니다."));
        // 대표 이미지 가져오기 (한 개만)
        String thumbnailImageUrl = productImageRepository.findByThumbnailImage(product.getId())
            .getImageUrl();

        return OrderItemGetResponse.from(orderItem, product, thumbnailImageUrl);
    }

    private List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findAllByOrdersId(orderId);
        // 주문 아이템이 없을 경우
        if (orderItems.isEmpty()) {
            throw new CommonException(BError.NOT_EXIST, "주문 아이템");
        }
        return orderItems;
    }

    private Orders getOrderById(Long orderId) {
        return ordersRepository.findById(orderId)
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "주문"));
    }

    // todo: 결제하기
    @Transactional
    public String payOrder(User user, Long orderId, Long userReceivedCouponId) {
        // 주문에 쿠폰 적용
        Orders order = getOrderById(orderId);
        // 이미 완료된 주문인지 확인
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new CommonException(BError.NOT_VALID, "이미 완료(확정)된 주문입니다.");
        }

        if (userReceivedCouponId != null) {
            UserReceivedCoupon coupon = getUserValidateReceivedCoupon(user, userReceivedCouponId);
            // 주문에 쿠폰 정보 저장
            order = ordersRepository.save(order.updateCoupon(coupon));
        }

        String pid = generatePaymentId();
        Integer quantity = 0;
        // 카카오 페이 API 호출
        KakaoPayReadyRequest request = kakaoPayClient.createKakaoPayReadyRequest(
            String.valueOf(orderId),
            user.getUuid(),
            "test",
            quantity,
            order.getTotalPrice().intValue(),
            pid
        );

        KakaoPayReadyResponse kakaoPayReadyResponse = kakaoPayClient.readyPayment(request);
        readyPayment(user, orderId, pid, order, kakaoPayReadyResponse.tid());

        return kakaoPayReadyResponse.next_redirect_pc_url();
    }

    private UserReceivedCoupon getUserValidateReceivedCoupon(User user, Long userReceivedCouponId) {
        UserReceivedCoupon coupon = userReceivedCouponRepository.findById(userReceivedCouponId)
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "쿠폰"));
        // 쿠폰 사용 여부 확인
        if (coupon.getIsUsed()) {
            throw new CommonException(BError.NOT_VALID, "이미 사용된 쿠폰입니다.");
        }
        // 쿠폰 소유자 확인
        if (!coupon.getUser().getId().equals(user.getId())) {
            throw new CommonException(BError.NOT_VALID, "쿠폰 소유자가 아닙니다.");
        }
        return coupon;
    }

    private void readyPayment(User user, Long orderId, String pid, Orders order, String tid) {
        Payment payment = Payment.ready(String.valueOf(orderId),
            pid,
            user.getUuid(),
            order.getTotalPrice().intValue(),
            tid);

        paymentRepository.save(payment);
    }

    @Transactional
    public void approvePayment(String pid, String token) {
        Payment payment = paymentRepository.findByPid(pid)
            .orElseThrow(
                () -> new CommonException(BError.NOT_EXIST, String.format("Payment %s", pid)));
        try {
            Long orderId = Long.valueOf(payment.getOrderId());
            User user = userRepository.findByUuid(payment.getUid())
                .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "user"));

            Orders order = getOrderById(orderId);
            // 이미 완료된 주문인지 확인
            if (order.getStatus() == OrderStatus.COMPLETED) {
                throw new CommonException(BError.NOT_VALID, "이미 완료(확정)된 주문입니다.");
            }
            List<OrderItem> orderItems = getOrderItemsByOrderId(orderId);

            orderItems.forEach(orderItem -> {
                // DB 락
                productRepository.findByIdWithLock(orderItem.getProduct().getId())
                    .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "상품이 존재하지 않습니다."));
                // 재고 확인
                if (orderItem.getProduct().getStock() < orderItem.getQuantity()) {
                    throw new CommonException(BError.NOT_VALID, "재고 부족");
                }
                // 재고 차감
                productRepository.save(orderItem.getProduct().updateStock(orderItem.getQuantity()));
            });
            // 주문 확정
            Orders savedOrder = ordersRepository.save(order.updateStatus(OrderStatus.COMPLETED));

            // 쿠폰 사용 처리, 장바구니 비우기
            clearCartAndDeleteCouponAsync(user, savedOrder);
            Payment approvePayment = payment.approvePayment();
            paymentRepository.save(approvePayment);

            // 카카오 한테 요청 보내기
            ApproveRequest approveRequest = ApproveRequest.builder()
                .cid("TC0ONETIME")
                .tid(payment.getTid())
                .partnerOrderId(payment.getOrderId())
                .partnerUserId(payment.getUid())
                .pgToken(token)
                .build();

            KakaoPayApproveResponse kakaoPayApproveResponse = kakaoPayClient.paymentApprove(approveRequest);
            log.info("Payment approved for pid: {}", pid);
        } catch (Exception e) {
            log.error("Failed to approve payment for pid: {}", pid, e);
        }
    }

    private String generatePaymentId() {
        return String.valueOf(UUID.randomUUID());
    }
}
