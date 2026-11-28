package com.madebywilfredo.payment.domain.exception;

import com.madebywilfredo.payment.domain.enums.PaymentStatus;

public class DomainValidationException extends  RuntimeException {
    public DomainValidationException (String message) {
        super(message);
    }

}

