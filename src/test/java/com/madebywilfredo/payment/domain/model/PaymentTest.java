package com.madebywilfredo.payment.domain.model;

import com.madebywilfredo.payment.domain.enums.PaymentStatus;
import com.madebywilfredo.payment.domain.exception.DomainValidationException;
import com.madebywilfredo.payment.domain.exception.InvalidPaymentTransitionException;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

public class PaymentTest {
    @Nested
    class CreationAndInvariantsTests {

        @Test
        public void deberiaCrearPagoConValoresInicialesCorrectos() {
            BigDecimal amount = new BigDecimal("1000.00");
            BigDecimal expectedTax = new BigDecimal("2.00");

            Payment payment = Payment.create(
                    "ACC-001",
                    "ACC-002",
                    amount,
                    "DOP");
            assertNotNull(payment.getId(), "El pago recién creado debe tener un ID único asignado");
            assertEquals(PaymentStatus.PENDING, (payment.getStatus()), "Todo pago nuevo debe nacer en estado PENDING");
            assertEquals(0, expectedTax.compareTo(payment.getTaxAmount()), "El impuesto calculado debe ser exactamente el 0.20% del monto");
            assertEquals(0, amount.compareTo(payment.getAmount()), "El monto del pago debe coincidir exactamente con el valor inicial");
            assertNull(payment.getBankSettledAmount(), "El monto liquidado por el banco debe ser null al crear el pago");
            assertEquals(0, payment.getReversalAttempts(), "El contador de reintentos de reverso debe iniciar en 0");
            assertEquals(1, payment.getAuditHistory().size(), "El historial de auditoría debe iniciar con exactamente 1 evento");
        }

        @Test
        public void deberiaLanzarExcepcionCuandoLosDatosSonInvalidos() {
            assertAll("Validaciones de creación de un pago. ",

                    () -> assertThrows(DomainValidationException.class, () ->
                                    Payment.create("ACC-001", "ACC-002", BigDecimal.ZERO, "DOP"),
                            "No se debe permitir crear un pago con monto igual a cero"),

                    () -> assertThrows(DomainValidationException.class, () ->
                                    Payment.create("ACC-001", "ACC-002", new BigDecimal("-50.00"), "DOP"),
                            "No se debe permitir crear un pago con monto negativo"),

                    () -> assertThrows(DomainValidationException.class, () ->
                                    Payment.create("ACC-001", "ACC-002", null, "DOP"),
                            "No se debe permitir crear un pago sin especificar un monto"),

                    () -> assertThrows(DomainValidationException.class, () ->
                                    Payment.create("ACC-001", "ACC-001", new BigDecimal("100.00"), "DOP"),
                            "No se debe permitir transferir fondos hacia la misma cuenta de origen"),

                    () -> assertThrows(DomainValidationException.class, () ->
                                    Payment.create(null, "ACC-002", new BigDecimal("100.00"), "DOP"),
                            "La cuenta de origen y la cuenta de destino son obligatorias"),

                    () -> assertThrows(DomainValidationException.class, () ->
                                    Payment.create("ACC-001", "ACC-002", new BigDecimal("100.00"), ""),
                            "La moneda de la transacción es obligatoria")
            );

        }


    }

    @Nested
    class HappyPathLifecycleTests {
        BigDecimal settledAmount = new BigDecimal("1000.00");

        @Test
        public void deberiaCompletarCicloDeVidaHastaConciliado() {

            Payment payment = createDefaultPayment();

            payment.validate();
            assertEquals(PaymentStatus.VALIDATED, payment.getStatus(),
                    "El pago debe pasar a estado VALIDATED tras validarse");

            payment.markAsSent("ACH-REF-999");
            assertEquals(PaymentStatus.SENT, payment.getStatus(),
                    "El pago debe pasar a estado SENT tras enviarse al banco");

            payment.settle(new BigDecimal("1000.00"));
            assertEquals(PaymentStatus.SETTLED, payment.getStatus(),
                    "El pago debe pasar a estado SETTLED tras la liquidación del banco");
            assertEquals(0, settledAmount.compareTo(payment.getBankSettledAmount()),
                    "El monto liquidado guardado debe coincidir con 1000.00");

            payment.evaluateReconciliation();
            assertEquals(PaymentStatus.RECONCILED, payment.getStatus(),
                    "El pago debe pasar a estado RECONCILED tras conciliarse");

            List<PaymentAuditEvent> history = payment.getAuditHistory();
            PaymentAuditEvent lastEvent = history.getLast();
            assertEquals(5, history.size(),
                    "El historial debe tener 5 eventos");
            assertAll("Verificación del último evento de auditoría",
                    () -> assertEquals(PaymentStatus.SETTLED, lastEvent.fromStatus()),
                    () -> assertEquals(PaymentStatus.RECONCILED, lastEvent.toStatus()),
                    () -> assertEquals("SYSTEM", lastEvent.actor()),
                    () -> assertNotNull(lastEvent.ocurrentAt()),
                    () -> assertFalse(lastEvent.reason().isBlank()));
        }

        @Test
        public void deberiaDevolverHistorialDeAuditoriaInmutable() {
            Payment payment = createDefaultPayment();
            List<PaymentAuditEvent> history = payment.getAuditHistory();
            assertThrows(UnsupportedOperationException.class, () ->
                    history.clear(), "El historial de auditoría debe ser inmutable");
        }
    }

    private Payment createDefaultPayment() {
        return Payment.create("ACC-001", "ACC-002", new BigDecimal("1000.00"), "DOP");
    }

    @Nested
    class DiscrepancyAndIllegalTransitionsTests{
        @Test
        public void deberiaDetectarDiscrepanciaCuandoLosMontosNoCuadran(){
            Payment payment = createDefaultPayment();
            payment.validate();
            payment.markAsSent("ACH-REF-999");
            payment.settle(new BigDecimal("999.99"));
            payment.evaluateReconciliation();
            assertEquals(PaymentStatus.DISCREPANCY, payment.getStatus(),
                    "El pago debe pasar a estado DISCREPANCY cuando el monto liquidado difiere del monto original");
        }
        @Test
        public void deberiaPermitirForzarConciliacionSoloConOperadorYMotivoValidos(){
            Payment payment = createDiscrepancyPayment();
            assertThrows(DomainValidationException.class,() -> {
                payment.forceReconcile("OP-01", "");
            });
            payment.forceReconcile("OP-01", "Ajuste aprobado");
            assertEquals(PaymentStatus.RECONCILED, payment.getStatus());
            assertEquals("OP-01", payment.getAuditHistory().getLast().actor());
        }


    }
    @Nested
    class ReversalBoundaryTests{
        @Test
        public void deberiaPermitirHastaTresReintentosDeReversoYBloquearElCuarto(){
            Payment payment = createDiscrepancyPayment();
            payment.initiateReversal("OP-01", "Devolución solicitada");
            payment.failReversal("Timeout red ACH");
            for (int i = 0; i < 3;  i++){
                payment.retryReversal("OP-01");
        payment.failReversal("Error técnico");}

        assertEquals(3, payment.getReversalAttempts(),
                "El contador debe registrar 3 reintentos");



            assertThrows(DomainValidationException.class, () ->
                    payment.retryReversal("OP-01"),
                    "No se debe permitir un cuarto reintento");
            payment.closeByAccountingAdjustment("OP-01", "Rechazo definitivo ACH");
            assertEquals(PaymentStatus.ACCOUNTING_ADJUSTMENT, payment.getStatus(),
                    "El pago debe cerrarse en ACCOUNTING_ADJUSTMENT");        }
    }
    private Payment createDiscrepancyPayment(){
        Payment payment = createDefaultPayment();
        payment.validate();
        payment.markAsSent("ACH-REF-999");
        payment.settle(new BigDecimal("999.99"));
        payment.evaluateReconciliation();
        return payment;
    }
    @Test
    public void deberiaBloquearSaltosIlegalesDeEstado(){
        Payment payment = createDefaultPayment();
        assertThrows(InvalidPaymentTransitionException.class, () ->
                payment.settle(new BigDecimal("1000.00")));
    }
}