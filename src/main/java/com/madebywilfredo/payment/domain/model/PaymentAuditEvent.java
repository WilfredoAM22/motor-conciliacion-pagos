package com.madebywilfredo.payment.domain.model;
import com.madebywilfredo.payment.domain.enums.PaymentStatus;
import java.time.Instant;

public record PaymentAuditEvent(
    PaymentStatus fromStatus,
    PaymentStatus toStatus,
    String actor,
    String reason,
    Instant ocurrentAt
)
{}