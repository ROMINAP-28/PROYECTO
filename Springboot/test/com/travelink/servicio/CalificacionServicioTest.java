package com.travelink.servicio;

import com.travelink.controlador.dto.CalificacionRequest;
import com.travelink.entidades.Calificacion;
import com.travelink.entidades.Reserva;
import com.travelink.repositorio.CalificacionRepositorio;
import com.travelink.repositorio.ReservaJpaRepositorio;
import com.travelink.servicio.impl.CalificacionServicioImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CalificacionServicioTest {

    @Mock
    private CalificacionRepositorio calificacionRepositorio;

    @Mock
    private ReservaJpaRepositorio reservaJpaRepositorio;

    @InjectMocks
    private CalificacionServicioImpl calificacionServicio;

    @Test
    @DisplayName("Debe registrar una calificación asociada a una reserva válida")
    void testRegistrarCalificacion() {
        CalificacionRequest req = new CalificacionRequest();
        req.setIdReserva(50);
        req.setEstrellas(5);
        req.setComentario("Excelente tour y atención");

        Reserva reserva = new Reserva();
        reserva.setIdReserva(50);
        reserva.setIdAgencia(3);

        when(reservaJpaRepositorio.findById(50)).thenReturn(Optional.of(reserva));
        when(calificacionRepositorio.save(any(Calificacion.class))).thenAnswer(i -> {
            Calificacion c = i.getArgument(0);
            c.setIdCalificacion(1);
            return c;
        });

        Calificacion guardada = calificacionServicio.registrarCalificacion(req, 12);

        assertNotNull(guardada);
        assertEquals(1, guardada.getIdCalificacion());
        assertEquals(12, guardada.getIdUsuario());
        assertEquals(3, guardada.getIdAgencia(), "Debe obtener el idAgencia de la reserva");
        assertEquals(5, guardada.getEstrellas());
        assertEquals("Excelente tour y atención", guardada.getComentario());

        verify(calificacionRepositorio, times(1)).save(any(Calificacion.class));
    }

    @Test
    @DisplayName("Debe calcular y redondear el promedio de estrellas de una agencia")
    void testObtenerPromedioAgencia() {
        when(calificacionRepositorio.promedioEstrellasPorAgencia(3)).thenReturn(4.6666);

        Double promedio = calificacionServicio.obtenerPromedioAgencia(3);

        assertEquals(4.7, promedio, 0.001, "Debe redondear a 1 decimal");
    }

    @Test
    @DisplayName("Si la agencia no tiene calificaciones, el promedio por defecto debe ser 5.0")
    void testObtenerPromedioAgenciaPorDefecto() {
        when(calificacionRepositorio.promedioEstrellasPorAgencia(99)).thenReturn(null);

        Double promedio = calificacionServicio.obtenerPromedioAgencia(99);

        assertEquals(5.0, promedio, 0.001);
    }

    @Test
    @DisplayName("Debe marcar como eliminada una calificación por parte del administrador")
    void testMarcarEliminadoPorAdmin() {
        Calificacion cal = new Calificacion();
        cal.setIdCalificacion(20);
        cal.setEliminadoPorAdmin(false);

        when(calificacionRepositorio.findById(20)).thenReturn(Optional.of(cal));
        when(calificacionRepositorio.save(any(Calificacion.class))).thenAnswer(i -> i.getArgument(0));

        boolean resultado = calificacionServicio.marcarEliminado(20, "Comentario con lenguaje inapropiado");

        assertTrue(resultado);
        assertTrue(cal.getEliminadoPorAdmin());
        assertEquals("Comentario con lenguaje inapropiado", cal.getMotivoEliminacion());
        verify(calificacionRepositorio, times(1)).save(cal);
    }
}
