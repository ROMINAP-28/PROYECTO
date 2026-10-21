package com.travelink.servicio;

import com.travelink.entidades.Destino;

import java.util.List;
import java.util.Optional;

/**
 * Interface del Servicio de Destinos Turísticos.
 * Aplica principios SOLID:
 * - SRP: Catálogo de regiones y destinos disponibles en el Perú.
 * - DIP: Abstracción inyectable en controladores.
 */
public interface DestinoServicio {

    List<Destino> listarTodos();

    List<Destino> listarActivos();

    Optional<Destino> buscarPorId(Integer id);

    Destino guardar(Destino destino);
}
