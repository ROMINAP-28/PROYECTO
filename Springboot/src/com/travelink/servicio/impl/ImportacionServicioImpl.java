package com.travelink.servicio.impl;

import com.travelink.controlador.dto.ImportacionResultadoDto;
import com.travelink.entidades.Tour;
import com.travelink.entidades.TourFecha;
import com.travelink.repositorio.TourFechaRepositorio;
import com.travelink.repositorio.TourRepositorio;
import com.travelink.servicio.ImportacionServicio;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Implementación del Servicio de Importación Masiva.
 * Aplica principios SOLID:
 * - SRP: Validación y persistencia atómica de fechas de salida importadas desde Excel o CSV.
 * - DIP: Inyección de dependencias por constructor.
 */
@Service
public class ImportacionServicioImpl implements ImportacionServicio {

    private static final Logger log = LoggerFactory.getLogger(ImportacionServicioImpl.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private final TourRepositorio tourRepositorio;
    private final TourFechaRepositorio tourFechaRepositorio;

    public ImportacionServicioImpl(TourRepositorio tourRepositorio,
                                   TourFechaRepositorio tourFechaRepositorio) {
        this.tourRepositorio = tourRepositorio;
        this.tourFechaRepositorio = tourFechaRepositorio;
    }

    @Override
    @Transactional
    public ImportacionResultadoDto importarDisponibilidad(MultipartFile file, int idAgencia) {
        ImportacionResultadoDto resultado = new ImportacionResultadoDto();

        if (file == null || file.isEmpty()) {
            resultado.setStatus("error");
            resultado.setMensaje("No se ha enviado ningún archivo para importar.");
            return resultado;
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        String lowerFilename = originalFilename.toLowerCase();
        long fileSize = file.getSize();

        log.info("[IMPORTACIÓN] Iniciando carga de disponibilidad por Agencia #{}. Archivo: '{}', Tamaño: {} bytes",
                idAgencia, originalFilename, fileSize);

        // 1. Validar tamaño máximo
        if (fileSize > MAX_FILE_SIZE) {
            String msg = String.format("El archivo excede el tamaño máximo permitido de 5 MB (tamaño actual: %.2f MB)",
                    fileSize / (1024.0 * 1024.0));
            log.warn("[IMPORTACIÓN] Rechazado por tamaño excesivo: {} bytes", fileSize);
            resultado.setStatus("error");
            resultado.setMensaje(msg);
            return resultado;
        }

        // 2. Validar extensión permitida
        boolean isExcel = lowerFilename.endsWith(".xlsx");
        boolean isCsv = lowerFilename.endsWith(".csv");

        if (!isExcel && !isCsv) {
            String msg = "Formato no permitido. Únicamente se aceptan archivos Excel (.xlsx) o CSV (.csv).";
            log.warn("[IMPORTACIÓN] Formato no soportado para archivo '{}'", originalFilename);
            resultado.setStatus("error");
            resultado.setMensaje(msg);
            return resultado;
        }

        // 3. Procesar según formato
        try {
            if (isExcel) {
                procesarExcel(file.getInputStream(), idAgencia, resultado);
            } else {
                procesarCsv(file.getInputStream(), idAgencia, resultado);
            }

            if (resultado.getFilasImportadas() > 0 && resultado.getFilasError() == 0) {
                resultado.setStatus("success");
                resultado.setMensaje(String.format("Importación completada con éxito: %d fecha(s) registrada(s).",
                        resultado.getFilasImportadas()));
            } else if (resultado.getFilasImportadas() > 0 && resultado.getFilasError() > 0) {
                resultado.setStatus("warning");
                resultado.setMensaje(String.format("Importación parcial: %d fecha(s) registrada(s), %d con error.",
                        resultado.getFilasImportadas(), resultado.getFilasError()));
            } else {
                resultado.setStatus("error");
                resultado.setMensaje(String.format("No se pudo importar ninguna fila. Se detectaron %d error(es).",
                        resultado.getFilasError()));
            }

            log.info("[IMPORTACIÓN] Finalizada Agencia #{}. Leídas: {}, Exitosas: {}, Errores: {}",
                    idAgencia, resultado.getTotalFilas(), resultado.getFilasImportadas(), resultado.getFilasError());

        } catch (Exception e) {
            log.error("[IMPORTACIÓN] Error fatal procesando archivo '{}': {}", originalFilename, e.getMessage(), e);
            resultado.setStatus("error");
            resultado.setMensaje("Error al procesar el archivo: " + e.getMessage());
        }

        return resultado;
    }

    private void procesarExcel(InputStream is, int idAgencia, ImportacionResultadoDto resultado) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("La primera hoja del libro Excel está vacía.");
            }

            int rowNum = 0;
            for (Row row : sheet) {
                rowNum++;
                // Saltar fila de encabezado
                if (rowNum == 1) continue;

                // Verificar si la fila está vacía
                if (esFilaVacia(row)) continue;

                resultado.setTotalFilas(resultado.getTotalFilas() + 1);

                String idTourStr = getCellStringValue(row.getCell(0));
                String fechaStr = getCellDateOrStringValue(row.getCell(1));
                String horaStr = getCellStringValue(row.getCell(2));
                String cupoStr = getCellStringValue(row.getCell(3));

                validarYGuardarFila(rowNum, idTourStr, fechaStr, horaStr, cupoStr, idAgencia, resultado);
            }
        }
    }

    private void procesarCsv(InputStream is, int idAgencia, ImportacionResultadoDto resultado) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            int lineNum = 0;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                // Saltar encabezado
                if (lineNum == 1) continue;

                line = line.trim();
                if (line.isEmpty()) continue;

                resultado.setTotalFilas(resultado.getTotalFilas() + 1);

                // Separador por coma o punto y coma
                String[] tokens = line.contains(";") ? line.split(";") : line.split(",");

                String idTourStr = tokens.length > 0 ? tokens[0].trim() : "";
                String fechaStr = tokens.length > 1 ? tokens[1].trim() : "";
                String horaStr = tokens.length > 2 ? tokens[2].trim() : "";
                String cupoStr = tokens.length > 3 ? tokens[3].trim() : "";

                validarYGuardarFila(lineNum, idTourStr, fechaStr, horaStr, cupoStr, idAgencia, resultado);
            }
        }
    }

    private void validarYGuardarFila(int fila, String idTourStr, String fechaStr, String horaStr,
                                     String cupoStr, int idAgencia, ImportacionResultadoDto resultado) {
        // Validar ID Tour
        if (idTourStr == null || idTourStr.isBlank()) {
            resultado.agregarError(fila, "El campo ID_TOUR es obligatorio.");
            log.warn("[IMPORTACIÓN] Fila {}: ID_TOUR vacío", fila);
            return;
        }

        int idTour;
        try {
            idTour = (int) Double.parseDouble(idTourStr);
        } catch (NumberFormatException e) {
            resultado.agregarError(fila, "ID_TOUR inválido ('" + idTourStr + "'). Debe ser numérico.");
            log.warn("[IMPORTACIÓN] Fila {}: ID_TOUR no numérico '{}'", fila, idTourStr);
            return;
        }

        // Validar que el tour exista y pertenezca a la agencia
        Optional<Tour> tourOpt = tourRepositorio.findById(idTour);
        if (tourOpt.isEmpty()) {
            resultado.agregarError(fila, "El Tour #" + idTour + " no existe en el sistema.");
            log.warn("[IMPORTACIÓN] Fila {}: Tour #{} no existe", fila, idTour);
            return;
        }

        Tour tour = tourOpt.get();
        if (idAgencia > 0 && tour.getIdAgencia() != null && tour.getIdAgencia() != idAgencia) {
            resultado.agregarError(fila, "El Tour #" + idTour + " ('" + tour.getNombre() + "') pertenece a otra agencia.");
            log.warn("[IMPORTACIÓN] Fila {}: Tour #{} no pertenece a agencia #{}", fila, idTour, idAgencia);
            return;
        }

        // Validar Fecha
        if (fechaStr == null || fechaStr.isBlank()) {
            resultado.agregarError(fila, "El campo FECHA es obligatorio.");
            return;
        }

        Date fechaParsed = parsearFecha(fechaStr);
        if (fechaParsed == null) {
            resultado.agregarError(fila, "Formato de FECHA inválido ('" + fechaStr + "'). Formato esperado: yyyy-MM-dd o dd/MM/yyyy.");
            log.warn("[IMPORTACIÓN] Fila {}: Fecha inválida '{}'", fila, fechaStr);
            return;
        }

        // Validar Hora
        if (horaStr == null || horaStr.isBlank()) {
            horaStr = "08:00";
        }
        Time horaParsed = parsearHora(horaStr);
        if (horaParsed == null) {
            resultado.agregarError(fila, "Formato de HORA inválido ('" + horaStr + "'). Formato esperado: HH:mm (ej. 08:30).");
            log.warn("[IMPORTACIÓN] Fila {}: Hora inválida '{}'", fila, horaStr);
            return;
        }

        // Validar Cupo
        int cupo = 20;
        if (cupoStr != null && !cupoStr.isBlank()) {
            try {
                cupo = (int) Double.parseDouble(cupoStr);
            } catch (NumberFormatException e) {
                resultado.agregarError(fila, "CUPO_TOTAL inválido ('" + cupoStr + "'). Debe ser un entero positivo.");
                return;
            }
        }
        if (cupo <= 0) {
            resultado.agregarError(fila, "El CUPO_TOTAL debe ser mayor a 0 (ingresado: " + cupo + ").");
            return;
        }

        // Guardar entidad TourFecha
        TourFecha tf = new TourFecha();
        tf.setTour(tour);
        tf.setIdTour(tour.getIdTour());
        tf.setFecha(fechaParsed);
        tf.setHoraInicio(horaParsed);
        tf.setCupoTotal(cupo);
        tf.setCupoDisponible(cupo);
        tf.setEstado("DISPONIBLE");

        tourFechaRepositorio.save(tf);
        resultado.incrementarImportadas();

        log.info("[IMPORTACIÓN] Fila {}: TourFecha #{} guardada (Tour #{}, Fecha: {}, Hora: {}, Cupo: {})",
                fila, tf.getIdTourFecha(), idTour, fechaStr, horaStr, cupo);
    }

    private Date parsearFecha(String fechaStr) {
        String[] formatos = {"yyyy-MM-dd", "dd/MM/yyyy", "d/M/yyyy", "yyyy/MM/dd"};
        for (String fmt : formatos) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(fmt);
                sdf.setLenient(false);
                return sdf.parse(fechaStr.trim());
            } catch (Exception ignored) {}
        }
        return null;
    }

    private Time parsearHora(String horaStr) {
        horaStr = horaStr.trim();
        if (horaStr.length() == 5) {
            horaStr = horaStr + ":00";
        }
        try {
            return Time.valueOf(horaStr);
        } catch (Exception ignored) {}
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date d = sdf.parse(horaStr);
            return new Time(d.getTime());
        } catch (Exception ignored) {}
        return null;
    }

    private boolean esFilaVacia(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private String getCellDateOrStringValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(cell.getDateCellValue());
        }
        return getCellStringValue(cell);
    }

    @Override
    public void generarPlantillaDisponibilidad(OutputStream os) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Plantilla_Disponibilidad");

            // Estilo encabezado
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID_TOUR", "FECHA (yyyy-MM-dd)", "HORA_INICIO (HH:mm)", "CUPO_TOTAL"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Filas de ejemplo
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue(1);
            r1.createCell(1).setCellValue("2026-11-15");
            r1.createCell(2).setCellValue("08:30");
            r1.createCell(3).setCellValue(25);

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue(1);
            r2.createCell(1).setCellValue("2026-11-20");
            r2.createCell(2).setCellValue("09:00");
            r2.createCell(3).setCellValue(30);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(os);
        }
    }
}
