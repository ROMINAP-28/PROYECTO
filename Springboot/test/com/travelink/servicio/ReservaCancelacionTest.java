package com.travelink.servicio;

import com.travelink.entidades.DetalleReserva;
import com.travelink.entidades.Reserva;
import com.travelink.entidades.Tour;
import com.travelink.repositorio.ReservaJpaRepositorio;
import com.travelink.repositorio.TourFechaRepositorio;
import com.travelink.servicio.impl.ReservaServicioImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la lógica de cancelación de reservas y devolución de cupos.
 */
@ExtendWith(MockitoExtension.class)
public class ReservaCancelacionTest {

    @Mock
    private ReservaJpaRepositorio reservaJpaRepositorio;

    @Mock
    private TourFechaRepositorio tourFechaRepositorio;

    @InjectMocks
    private ReservaServicioImpl reservaServicio;

    private Reserva reservaActiva;

    @BeforeEach
    void setUp() {
        reservaActiva = new Reserva();
        reservaActiva.setIdReserva(200);
        reservaActiva.setIdUsuario(10);
        reservaActiva.setIdAgencia(3);
        reservaActiva.setIdTourFecha(50);
        reservaActiva.setCodigoReserva("TRV-CANC-001");
        reservaActiva.setEstado("CONFIRMADA");
        reservaActiva.setTotal(new BigDecimal("500.00"));

        Tour tour = new Tour();
        tour.setIdTour(5);
        tour.setAceptaBebes(false);
        tour.setPrecioBebe(BigDecimal.ZERO);
        reservaActiva.setTour(tour);

        DetalleReserva detalle = new DetalleReserva();
        detalle.setCantAdultos(2);
        detalle.setCantNinos(1);
        detalle.setCantBebes(0);
        reservaActiva.setDetalles(Collections.singletonList(detalle));
    }

    @Test
    @DisplayName("CP002-01: Cancelar reserva activa debe cambiar estado a 'Cancelada' y devolver cupos")
    void testCancelarReservaActivaExito() {
        when(reservaJpaRepositorio.findById(200)).thenReturn(Optional.of(reservaActiva));
        when(tourFechaRepositorio.devolverCupo(eq(50), eq(3))).thenReturn(1);
        when(reservaJpaRepositorio.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));

        boolean resultado = reservaServicio.cancelar(200, "Cambio de itinerario de viaje");

        assertTrue(resultado, "Debe retornar true al cancelar exitosamente");
        assertEquals("Cancelada", reservaActiva.getEstado());
        assertEquals("Cambio de itinerario de viaje", reservaActiva.getMotivoCancelacion());
        verify(tourFechaRepositorio, times(1)).devolverCupo(50, 3);
        verify(reservaJpaRepositorio, times(1)).save(reservaActiva);
    }

    @Test
    @DisplayName("CP002-02: Cancelar reserva inexistente debe retornar false")
    void testCancelarReservaInexistente() {
        when(reservaJpaRepositorio.findById(9999)).thenReturn(Optional.empty());

        boolean resultado = reservaServicio.cancelar(9999, "No existe");

        assertFalse(resultado, "Debe retornar false para ID de reserva no encontrado");
        verify(tourFechaRepositorio, never()).devolverCupo(anyInt(), anyInt());
        verify(reservaJpaRepositorio, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("CP002-03: Idempotencia - Cancelar reserva ya cancelada no debe duplicar devolución de cupos")
    void testCancelarReservaYaCanceladaNoDevuelveCupos() {
        reservaActiva.setEstado("Cancelada");
        when(reservaJpaRepositorio.findById(200)).thenReturn(Optional.of(reservaActiva));

        boolean resultado = reservaServicio.cancelar(200, "Intento duplicado");

        assertTrue(resultado, "Debe retornar true indicando que ya está cancelada");
        verify(tourFechaRepositorio, never()).devolverCupo(anyInt(), anyInt());
        verify(reservaJpaRepositorio, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("CP002-04: Cancelar con bebés en tour donde ocupan cupo (precio > 0) debe sumar bebés a cupos")
    void testCancelarConBebesOcupandoCupo() {
        reservaActiva.getTour().setAceptaBebes(true);
        reservaActiva.getTour().setPrecioBebe(new BigDecimal("30.00"));
        reservaActiva.getDetalles().get(0).setCantBebes(2);

        when(reservaJpaRepositorio.findById(200)).thenReturn(Optional.of(reservaActiva));
        when(tourFechaRepositorio.devolverCupo(eq(50), eq(5))).thenReturn(1); // 2 adultos + 1 niño + 2 bebés
        when(reservaJpaRepositorio.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));

        boolean resultado = reservaServicio.cancelar(200, "Problemas de salud");

        assertTrue(resultado);
        verify(tourFechaRepositorio, times(1)).devolverCupo(50, 5);
    }

    @Test
    @DisplayName("CP002-05: Cancelar con bebés en tour gratis no debe sumar bebés a los cupos devueltos")
    void testCancelarConBebesGratisNoOcupanCupo() {
        reservaActiva.getTour().setAceptaBebes(true);
        reservaActiva.getTour().setPrecioBebe(BigDecimal.ZERO);
        reservaActiva.getDetalles().get(0).setCantBebes(2);

        when(reservaJpaRepositorio.findById(200)).thenReturn(Optional.of(reservaActiva));
        when(tourFechaRepositorio.devolverCupo(eq(50), eq(3))).thenReturn(1); // 2 adultos + 1 niño + 0 bebés
        when(reservaJpaRepositorio.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));

        boolean resultado = reservaServicio.cancelar(200, "Cancelación de vacaciones");

        assertTrue(resultado);
        verify(tourFechaRepositorio, times(1)).devolverCupo(50, 3);
    }

    @Test
    @DisplayName("CP002-06: Cancelar con motivo vacío o nulo no debe generar excepción")
    void testCancelarConMotivoNulo() {
        when(reservaJpaRepositorio.findById(200)).thenReturn(Optional.of(reservaActiva));
        when(tourFechaRepositorio.devolverCupo(anyInt(), anyInt())).thenReturn(1);
        when(reservaJpaRepositorio.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));

        boolean resultado = reservaServicio.cancelar(200, null);

        assertTrue(resultado);
        assertEquals("Cancelada", reservaActiva.getEstado());
        assertNull(reservaActiva.getMotivoCancelacion());
    }
}

