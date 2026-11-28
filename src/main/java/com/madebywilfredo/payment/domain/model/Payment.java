package com.madebywilfredo.payment.domain.model;

import com.madebywilfredo.payment.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class Payment {
private Payment(){}
    public static Payment create(
        String sourceAccount,
        String destinationAccount,
        BigDecimal amount,
        String currency){
    throw new UnsupportedOperationException("Not implemented yet");
}

    public void validate(){throw new UnsupportedOperationException("Not implemented yet");}
    public void reject(String reason){throw new UnsupportedOperationException("Not implemented yet");}
    public void markAsSent (String bankReference){throw new UnsupportedOperationException("Not implemented yet");}
    public void settle ( BigDecimal bankSettledAmount){throw new UnsupportedOperationException("Not implemented yet");}
    public void markAsFailed  (String bankReason){throw new UnsupportedOperationException("Not implemented yet");}
    public void markAsUncertain (){throw new UnsupportedOperationException("Not implemented yet");}
    public void evaluateReconciliation  (){throw new UnsupportedOperationException("Not implemented yet");}
    public void forceReconcile (String operatorId, String auditReason){throw new UnsupportedOperationException("Not implemented yet");}
    public void initiateReversal(String operatorId, String reason){throw new UnsupportedOperationException("Not implemented yet");}
    public void completeReversal(){throw new UnsupportedOperationException("Not implemented yet");}
    public void failReversal(String technicalError){throw new UnsupportedOperationException("Not implemented yet");}
    public void retryReversal(String operatorId){throw new UnsupportedOperationException("Not implemented yet");}
    public void markReversalUncertain(){throw new UnsupportedOperationException("Not implemented yet");}
    public void closeByAccountingAdjustment (String operatorId, String auditReason){throw new UnsupportedOperationException("Not implemented yet");}

    public UUID getId(){throw new UnsupportedOperationException("Not implemented yet");}
    public PaymentStatus getStatus(){throw new UnsupportedOperationException("Not implemented yet");}
    public BigDecimal getAmount (){throw new UnsupportedOperationException("Not implemented yet");}
    public BigDecimal getTaxAmount(){throw new UnsupportedOperationException("Not implemented yet");}
    public BigDecimal getBankSettledAmount(){throw new UnsupportedOperationException("Not implemented yet");}
    public int getReversalAttempts(){throw new UnsupportedOperationException("Not implemented yet");}
    public List<PaymentAuditEvent> getAuditHistory(){throw new UnsupportedOperationException("Not implemented yet");}



}
