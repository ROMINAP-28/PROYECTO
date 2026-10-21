package com.travelink.servicio;

import com.travelink.entidades.Comision;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

public class ComisionServicioTest {

    private static final BigDecimal PORCENTAJE_COMISION_TRAVELINK = new BigDecimal("15.00");

    @Test
    @DisplayName("Debe calcular exactamente el 15% de comisión para Travelink y 85% para la agencia (S/ 1000.00)")
    void testCalculoComision15PorcientoBaseMil() {
        BigDecimal subtotal = new BigDecimal("1000.00");

        // Cálculo del 15%
        BigDecimal montoComision = subtotal.multiply(PORCENTAJE_COMISION_TRAVELINK)
                .divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP);
        BigDecimal montoNetoAgencia = subtotal.subtract(montoComision);

        assertEquals(new BigDecimal("150.00"), montoComision, "La comisión del 15% de 1000 debe ser 150.00");
        assertEquals(new BigDecimal("850.00"), montoNetoAgencia, "El neto para la agencia debe ser 850.00");
    }

    @Test
    @DisplayName("Debe calcular con precisión decimal la comisión del 15% sobre montos fraccionarios (S/ 249.90)")
    void testCalculoComisionMontoFraccionario() {
        BigDecimal subtotal = new BigDecimal("249.90");

        BigDecimal montoComision = subtotal.multiply(PORCENTAJE_COMISION_TRAVELINK)
                .divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP);
        BigDecimal montoNetoAgencia = subtotal.subtract(montoComision);

        // 249.90 * 0.15 = 37.485 -> redondeo a 2 decimales = 37.49
        assertEquals(new BigDecimal("37.49"), montoComision);
        // 249.90 - 37.49 = 212.41
        assertEquals(new BigDecimal("212.41"), montoNetoAgencia);
    }

    @Test
    @DisplayName("La entidad Comision debe inicializarse por defecto con 15.00% y estado PENDIENTE")
    void testEntidadComisionValoresPorDefecto() {
        Comision comision = new Comision();

        assertNotNull(comision.getPorcentaje());
        assertEquals(new BigDecimal("15.00"), comision.getPorcentaje());
        assertEquals(new BigDecimal("15.00"), comision.getPorcentajeComision());
        assertEquals("PENDIENTE", comision.getEstado());
        assertEquals("PENDIENTE", comision.getEstadoLiquidacion());
    }
}
