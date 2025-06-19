package com.sellect.server.payment.event;


import com.sellect.server.auth.domain.User;
import com.sellect.server.order.domain.Orders;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class KakaoPayReadyEvent extends ApplicationEvent {

    private final User user;
    private final Long orderId;
    private final Orders order;
    private final CompletableFuture<String> future;

    public KakaoPayReadyEvent(Object source, User user, Long orderId, Orders order,
        CompletableFuture<String> future) {
        super(source);
        this.user = user;
        this.orderId = orderId;
        this.order = order;
        this.future = future;
    }
}
