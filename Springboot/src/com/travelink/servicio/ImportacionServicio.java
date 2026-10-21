package com.travelink.servicio;

import com.travelink.controlador.dto.ImportacionResultadoDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;

/**
 * Interface del Servicio de Importación Masiva.
 * Aplica principios SOLID:
 * - SRP: Procesamiento y validación de importaciones masivas de disponibilidad/fechas.
 * - DIP: Abstracción desacoplada de la capa web.
 */
public interface ImportacionServicio {

    ImportacionResultadoDto importarDisponibilidad(MultipartFile file, int idAgencia);

    void generarPlantillaDisponibilidad(OutputStream os) throws Exception;
}
