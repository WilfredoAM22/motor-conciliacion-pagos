package com.madebywilfredo.payment.domain.enums;
import static com.madebywilfredo.payment.domain.enums.PaymentStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class PaymentStatusTest {

private record Transicion (PaymentStatus origen, PaymentStatus destino){}
    private static final Set<Transicion> TRANSICIONES_VALIDAS = Set.of(
            new Transicion(PENDING, VALIDATED),
            new Transicion(PENDING, REJECTED),
            new Transicion(VALIDATED, SENT),
            new Transicion(SENT, SETTLED),
            new Transicion(SENT, UNCERTAIN),
            new Transicion(SENT,FAILED),
            new Transicion(UNCERTAIN, SETTLED),
            new Transicion(UNCERTAIN, FAILED),
            new Transicion(SETTLED, RECONCILED),
            new Transicion(SETTLED, DISCREPANCY),
            new Transicion(DISCREPANCY, RECONCILED),
            new Transicion(DISCREPANCY,REVERSING),
            new Transicion(REVERSING, REVERSED),
            new Transicion(REVERSING, REVERSAL_FAILED),
            new Transicion(REVERSAL_FAILED, REVERSING),
            new Transicion(REVERSAL_FAILED, REVERSAL_UNCERTAIN),
            new Transicion(REVERSAL_FAILED, ACCOUNTING_ADJUSTMENT),
            new Transicion(REVERSAL_UNCERTAIN, REVERSED),
            new Transicion(REVERSAL_UNCERTAIN, ACCOUNTING_ADJUSTMENT)

    );
@Test
    void verificarCantidadDeEstadosYEstadosTerminales(){
        int cantidadEsperda = 14;
        int cantidadActual = PaymentStatus.values().length;
        assertEquals(cantidadEsperda, cantidadActual,
        " El enum PaymentStatus deberia tener 14 valores. ");

        Set<PaymentStatus> estadosFinales = Set.of(
                REJECTED,
                FAILED,
                RECONCILED,
                REVERSED,
                ACCOUNTING_ADJUSTMENT
        );
        assertEquals(5, estadosFinales.size(), " El enum PaymentStatus debe de tener 5 estados finales. ");

        int contadorTerminales = 0;
        int contadorNoTerminales = 0;

        for (PaymentStatus status : PaymentStatus.values()){
            if (estadosFinales.contains(status)){
                assertTrue(status.isTerminal(),
                        " El estado final " + status + " deberia devolver true en isTerminal(). ");
                contadorTerminales++;}
            else {
                assertFalse(status.isTerminal(),
                        " El estado no final " + status + " deberia devolver false en isTerminal(). ");
                contadorNoTerminales++;
            }
        }
        assertEquals(5, contadorTerminales, " El enum PaymentStatus debe de tener 5 estados finales. ");
        assertEquals(9, contadorNoTerminales, " El enum PaymentStatus debe de tener 9 estados no finales. ");
    }

    @Test
    void verificarTransicionDesdePendingANullDevuelveFalse(){
        boolean resultado = PaymentStatus.PENDING.canTransitionTo(null);
        assertFalse(resultado, " El estado PENDING no deberia poder transicionar a null. ");
    }

    @Test
    void verificarTodasLasTransiciones(){
    assertEquals(19, TRANSICIONES_VALIDAS.size(),
            " El conjunto debe de tener exactamente 19 transiciones validas ");

    for (PaymentStatus origen: PaymentStatus.values()){
        for (PaymentStatus destino: PaymentStatus.values()){
            Transicion parActual = new Transicion(origen, destino);
            boolean esValida = TRANSICIONES_VALIDAS.contains(parActual);

            if (esValida){
                assertTrue(origen.canTransitionTo(destino),
                        "La transición válida " + origen + " -> " + destino + " debería devolver true. ");
            }
            else {
               assertFalse(origen.canTransitionTo(destino),
                        "La transición inválida " + origen + " -> " + destino + " debería devolver false. ");
            }
        }
    }

    }

}
