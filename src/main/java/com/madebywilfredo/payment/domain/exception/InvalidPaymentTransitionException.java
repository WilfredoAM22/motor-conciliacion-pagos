package com.madebywilfredo.payment.domain.exception;

import com.madebywilfredo.payment.domain.enums.PaymentStatus;

public class InvalidPaymentTransitionException extends RuntimeException {
    private final PaymentStatus currentStatus;
    private final PaymentStatus attemptedStatus;

    public InvalidPaymentTransitionException(PaymentStatus currentStatus, PaymentStatus attemptedStatus ){
        super(String.format("Transición ilegal de %s a %s", currentStatus, attemptedStatus));
        this.currentStatus =currentStatus;
        this.attemptedStatus = attemptedStatus;
    }
    public PaymentStatus getCurrentStatus(){
        return this.currentStatus;
    }
    public PaymentStatus getAttemptedStatus(){
        return this.attemptedStatus;
    }

}