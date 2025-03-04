package com.sellect.server.payment.domain;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PaymentTest {

    @Test
    @DisplayName("결제 승인")
    void approvePayment() {
        Payment payment = Payment.ready("orderId", "pid", "uid", 1000, "tid");
        Payment approvePayment = payment.approvePayment();
        assertEquals(PaymentStatus.APPROVE, approvePayment.getStatus());
    }

    @Nested
    @DisplayName("결제 상태 변경 테스트")
    class PaymentStatusChangeTest{
        @Test
        @DisplayName("대기상태에서 결제 승인으로 변경한다.")
        void paymentApproveSuccess() {
            //given
            Payment payment = Payment.ready("orderId", "pid", "uid", 1000, "tid");
            //when
            Payment approvePayment = payment.approvePayment();
            //then
            then(approvePayment.getStatus()).isEqualTo(PaymentStatus.APPROVE);

        }

        @Test
        @DisplayName("대기상태에서 결제 취소로 변경한다.")
        void paymentFailSuccess() {
            //given
            Payment payment = Payment.ready("orderId", "pid", "uid", 1000, "tid");
            //when
            Payment failedPayment = payment.failPayment();
            //then
            then(failedPayment.getStatus()).isEqualTo(PaymentStatus.FAIL);

        }

        @Test
        @DisplayName("대기 상태에서 결제 캔슬 상태로 변경한다.")
        void paymentCancelSuccess() {
            //given
            Payment payment = Payment.ready("orderId", "pid", "uid", 1000, "tid");
            //when
            Payment cancelledPayment = payment.cancelPayment();
            //then
            then(cancelledPayment.getStatus()).isEqualTo(PaymentStatus.CANCEL);
        }

        @Test
        @DisplayName("대기 상태가 아닌 경우 approve 호출 시 상태가 변경되지 않음")
        void approvePaymentDoesNotChangeNonPendingStatus() {
            // given
            Payment payment = Payment.ready("orderId", "pid", "uid", 1000, "tid");
            Payment approvePayment = payment.approvePayment();// 먼저 APPROVE로 상태 변경
            PaymentStatus initialStatus = approvePayment.getStatus();

            // when
            Payment result = approvePayment.approvePayment();

            // then
            then(result.getStatus()).isEqualTo(initialStatus);
            then(result.getStatus()).isNotEqualTo(PaymentStatus.READY);
        }

        @Test
        @DisplayName("대기 상태가 아닌 경우 fail 호출 시 상태가 변경되지 않음")
        void failPaymentDoesNotChangeNonPendingStatus() {
            // given
            Payment payment = Payment.ready("orderId", "pid", "uid", 1000, "tid");
            Payment failedPayment = payment.failPayment();// 먼저 CANCEL로 상태 변경
            PaymentStatus initialStatus = failedPayment.getStatus();

            // when
            Payment result = failedPayment;

            // then
            then(result.getStatus()).isEqualTo(initialStatus);
            then(result.getStatus()).isNotEqualTo(PaymentStatus.READY);
        }
    }
}