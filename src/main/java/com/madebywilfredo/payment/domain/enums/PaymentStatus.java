package com.madebywilfredo.payment.domain.enums;
import java.util.Set;
public enum PaymentStatus {
    REJECTED,
    FAILED,
    RECONCILED,
    REVERSED,
    ACCOUNTING_ADJUSTMENT,

    PENDING{
       public Set<PaymentStatus>allowedTransitions(){return Set.of(VALIDATED, REJECTED);} },
    VALIDATED{
        public Set<PaymentStatus>allowedTransitions(){return Set.of(SENT);}},
    SENT{
        public Set<PaymentStatus>allowedTransitions(){return Set.of(SETTLED, FAILED, UNCERTAIN);} },
    SETTLED{
        public Set<PaymentStatus>allowedTransitions(){return Set.of(RECONCILED, DISCREPANCY);}},
    UNCERTAIN{
        public Set<PaymentStatus>allowedTransitions(){return Set.of(SETTLED, FAILED);}},
    DISCREPANCY{
        public Set<PaymentStatus>allowedTransitions(){return Set.of(RECONCILED, REVERSING);}},
    REVERSING{
        public Set<PaymentStatus>allowedTransitions(){return Set.of(REVERSED, REVERSAL_FAILED);}},
    REVERSAL_FAILED{
        public Set<PaymentStatus>allowedTransitions(){return Set.of(REVERSING, REVERSAL_UNCERTAIN, ACCOUNTING_ADJUSTMENT);}},
    REVERSAL_UNCERTAIN{
        public Set<PaymentStatus>allowedTransitions(){return Set.of(REVERSED, ACCOUNTING_ADJUSTMENT);}};

    public Set<PaymentStatus>allowedTransitions(){
      return Set.of();

  }
  public boolean canTransitionTo(PaymentStatus nextStatus) {
return nextStatus != null && allowedTransitions().contains(nextStatus);
}

public boolean isTerminal(){
    return allowedTransitions().isEmpty();
}
}


//¿Qué hace este bloque de codigo?
//En conjunto, verifica dos cosas: que el destino no sea nulo y
// que ese destino esté dentro de las transiciones permitidas del estado actual.
//ATT WILFREDO