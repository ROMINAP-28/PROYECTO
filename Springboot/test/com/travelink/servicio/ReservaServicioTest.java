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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservaServicioTest {

    @Mock
    private ReservaJpaRepositorio reservaJpaRepositorio;

    @Mock
    private TourFechaRepositorio tourFechaRepositorio;

    @InjectMocks
    private ReservaServicioImpl reservaServicio;

    private Reserva reservaPrueba;

    @BeforeEach
    void setUp() {
        reservaPrueba = new Reserva();
        reservaPrueba.setIdReserva(101);
        reservaPrueba.setIdUsuario(5);
        reservaPrueba.setIdAgencia(2);
        reservaPrueba.setIdTourFecha(45);
        reservaPrueba.setCodigoReserva("TRV-TEST-001");
        reservaPrueba.setEstado("CONFIRMADA");
        reservaPrueba.setTotal(new BigDecimal("350.00"));

        Tour tour = new Tour();
        tour.setIdTour(10);
        tour.setAceptaBebes(false);
        reservaPrueba.setTour(tour);

        DetalleReserva detalle = new DetalleReserva();
        detalle.setCantAdultos(2);
        detalle.setCantNinos(1);
        detalle.setCantBebes(0);
        reservaPrueba.setDetalles(Collections.singletonList(detalle));
    }

    @Test
    @DisplayName("Debe descontar cupos de forma atómica")
    void testDescontarCupoAtomico() {
        when(tourFechaRepositorio.descontarCupo(45, 3)).thenReturn(1);

        int filasAfectadas = reservaServicio.descontarCupoAtomico(45, 3);

        assertEquals(1, filasAfectadas, "Debe descontar 1 fila de fecha de tour");
        verify(tourFechaRepositorio, times(1)).descontarCupo(45, 3);
    }

    @Test
    @DisplayName("Debe cancelar una reserva activa y devolver los cupos correspondientes")
    void testCancelarReservaExitosa() {
        when(reservaJpaRepositorio.findById(101)).thenReturn(Optional.of(reservaPrueba));
        when(tourFechaRepositorio.devolverCupo(eq(45), eq(3))).thenReturn(1);
        when(reservaJpaRepositorio.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean resultado = reservaServicio.cancelar(101, "Emergencia médica");

        assertTrue(resultado, "La cancelación debe retornar true");
        assertEquals("Cancelada", reservaPrueba.getEstado(), "El estado debe cambiar a Cancelada");
        assertEquals("Emergencia médica", reservaPrueba.getMotivoCancelacion());

        verify(tourFechaRepositorio, times(1)).devolverCupo(45, 3);
        verify(reservaJpaRepositorio, times(1)).save(reservaPrueba);
    }

    @Test
    @DisplayName("Cancelar una reserva inexistente debe retornar false")
    void testCancelarReservaNoExiste() {
        when(reservaJpaRepositorio.findById(999)).thenReturn(Optional.empty());

        boolean resultado = reservaServicio.cancelar(999, "No existe");

        assertFalse(resultado, "Debe retornar false si la reserva no existe");
        verify(tourFechaRepositorio, never()).devolverCupo(anyInt(), anyInt());
    }
}
