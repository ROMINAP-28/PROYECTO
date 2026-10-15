package com.travelink.reportes;

import com.travelink.config.ConexionDB;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ExcelReporteService {

    /**
     * 1. AGENCIA: Reservas con filtros opcionales de fecha y estado.
     */
    public void generarReservasAgenciaExcel(int idAgencia, String fechaInicio, String fechaFin, String estado, OutputStream out) throws Exception {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        try {
            SXSSFSheet sheet = workbook.createSheet("Reservas");
            sheet.trackAllColumnsForAutoSizing();

            CellStyle styleHeader = createHeaderStyle(workbook, IndexedColors.DARK_BLUE.getIndex());
            CellStyle styleData = createDataStyle(workbook);
            CellStyle styleCurrency = createCurrencyStyle(workbook);
            CellStyle styleCenter = createCenterStyle(workbook);

            String[] headers = {
                "ID Reserva", "Código", "Fecha Registro", "Tour", "Fecha Salida", "Hora Salida",
                "Turista / Titular", "Adultos", "Niños", "Bebés", "Total Pasajeros", "Total (S/)", "Estado Reserva", "Estado Pago"
            };

            SXSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                SXSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(styleHeader);
            }

            StringBuilder sql = new StringBuilder("""
                SELECT
                    r.idReserva,
                    r.codigoReserva,
                    r.fechaRegistro,
                    COALESCE(r.nombreTour, t.nombre, 'Tour Turístico') AS tourNombre,
                    COALESCE(tf.fecha, r.fechaInicio) AS fechaSalida,
                    TIME_FORMAT(tf.horaInicio, '%H:%i') AS horaSalida,
                    COALESCE(CONCAT(p.nombre, ' ', p.apellidoPaterno), u.nombreUsuario, 'Turista') AS titularNombre,
                    COALESCE(dr.cantAdultos, 1) AS cantAdultos,
                    COALESCE(dr.cantNinos, 0) AS cantNinos,
                    COALESCE(dr.cantBebes, 0) AS cantBebes,
                    r.total,
                    r.estado AS estadoReserva,
                    COALESCE(
                        (SELECT pg.estado FROM Pago pg WHERE pg.idReserva = r.idReserva ORDER BY pg.idPago DESC LIMIT 1),
                        (SELECT mp.estadoPago FROM MetodoPago mp WHERE mp.idReserva = r.idReserva ORDER BY mp.idMetodoPago DESC LIMIT 1),
                        'COMPLETADO'
                    ) AS estadoPago
                FROM Reserva r
                LEFT JOIN Tour t ON r.idTour = t.idTour
                LEFT JOIN TourFecha tf ON r.idTourFecha = tf.idTourFecha
                LEFT JOIN Usuario u ON r.idUsuario = u.idUsuario
                LEFT JOIN Persona p ON u.idPersona = p.idPersona
                LEFT JOIN DetalleReserva dr ON r.idReserva = dr.idReserva AND (dr.tipo IS NULL OR dr.tipo = 'ADULTO')
                WHERE r.idAgencia = ?
            """);

            if (fechaInicio != null && !fechaInicio.isBlank()) {
                sql.append(" AND DATE(COALESCE(tf.fecha, r.fechaInicio, r.fechaRegistro)) >= ? ");
            }
            if (fechaFin != null && !fechaFin.isBlank()) {
                sql.append(" AND DATE(COALESCE(tf.fecha, r.fechaInicio, r.fechaRegistro)) <= ? ");
            }
            if (estado != null && !estado.isBlank() && !"todos".equalsIgnoreCase(estado)) {
                sql.append(" AND UPPER(r.estado) = ? ");
            }
            sql.append(" ORDER BY r.idReserva DESC");

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql.toString())) {
                int paramIdx = 1;
                ps.setInt(paramIdx++, idAgencia);
                if (fechaInicio != null && !fechaInicio.isBlank()) ps.setString(paramIdx++, fechaInicio.trim());
                if (fechaFin != null && !fechaFin.isBlank()) ps.setString(paramIdx++, fechaFin.trim());
                if (estado != null && !estado.isBlank() && !"todos".equalsIgnoreCase(estado)) ps.setString(paramIdx++, estado.trim().toUpperCase());

                try (ResultSet rs = ps.executeQuery()) {
                    int rowNum = 1;
                    while (rs.next()) {
                        SXSSFRow row = sheet.createRow(rowNum++);
                        int c = 0;
                        createCell(row, c++, rs.getInt("idReserva"), styleCenter);
                        createCell(row, c++, rs.getString("codigoReserva"), styleCenter);
                        createCell(row, c++, String.valueOf(rs.getTimestamp("fechaRegistro")), styleCenter);
                        createCell(row, c++, rs.getString("tourNombre"), styleData);
                        createCell(row, c++, String.valueOf(rs.getDate("fechaSalida")), styleCenter);
                        createCell(row, c++, rs.getString("horaSalida") != null ? rs.getString("horaSalida") : "08:00", styleCenter);
                        createCell(row, c++, rs.getString("titularNombre"), styleData);

                        int ad = rs.getInt("cantAdultos");
                        int ni = rs.getInt("cantNinos");
                        int be = rs.getInt("cantBebes");
                        createCell(row, c++, ad, styleCenter);
                        createCell(row, c++, ni, styleCenter);
                        createCell(row, c++, be, styleCenter);
                        createCell(row, c++, (ad + ni + be), styleCenter);

                        BigDecimal total = rs.getBigDecimal("total");
                        createCurrencyCell(row, c++, total != null ? total.doubleValue() : 0.0, styleCurrency);
                        createCell(row, c++, rs.getString("estadoReserva"), styleCenter);
                        createCell(row, c++, rs.getString("estadoPago"), styleCenter);
                    }
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
        } finally {
            workbook.dispose();
            workbook.close();
        }
    }

    /**
     * 2. AGENCIA: Pagos con filtros opcionales de fecha y estado.
     */
    public void generarPagosAgenciaExcel(int idAgencia, String fechaInicio, String fechaFin, String estado, OutputStream out) throws Exception {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        try {
            SXSSFSheet sheet = workbook.createSheet("Pagos");
            sheet.trackAllColumnsForAutoSizing();

            CellStyle styleHeader = createHeaderStyle(workbook, IndexedColors.SEA_GREEN.getIndex());
            CellStyle styleData = createDataStyle(workbook);
            CellStyle styleCurrency = createCurrencyStyle(workbook);
            CellStyle styleCenter = createCenterStyle(workbook);

            String[] headers = {
                "ID Pago", "N° Operación", "Código Reserva", "Tour", "Fecha Pago", "Método Pago",
                "Monto Total (S/)", "Comisión Travelink 15% (S/)", "Neto Agencia 85% (S/)", "Estado"
            };

            SXSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                SXSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(styleHeader);
            }

            StringBuilder sql = new StringBuilder("""
                SELECT
                    p.idPago,
                    COALESCE(p.numeroOperacion, r.codigoReserva, '-') AS numeroOperacion,
                    r.codigoReserva,
                    COALESCE(r.nombreTour, t.nombre, 'Tour Turístico') AS tourNombre,
                    p.fechaPago,
                    p.metodoPago,
                    p.monto,
                    p.estado
                FROM Pago p
                INNER JOIN Reserva r ON r.idReserva = p.idReserva
                LEFT JOIN Tour t ON r.idTour = t.idTour
                WHERE r.idAgencia = ?
            """);

            if (fechaInicio != null && !fechaInicio.isBlank()) {
                sql.append(" AND DATE(p.fechaPago) >= ? ");
            }
            if (fechaFin != null && !fechaFin.isBlank()) {
                sql.append(" AND DATE(p.fechaPago) <= ? ");
            }
            if (estado != null && !estado.isBlank() && !"todos".equalsIgnoreCase(estado)) {
                sql.append(" AND UPPER(p.estado) = ? ");
            }
            sql.append(" ORDER BY p.fechaPago DESC, p.idPago DESC");

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql.toString())) {
                int paramIdx = 1;
                ps.setInt(paramIdx++, idAgencia);
                if (fechaInicio != null && !fechaInicio.isBlank()) ps.setString(paramIdx++, fechaInicio.trim());
                if (fechaFin != null && !fechaFin.isBlank()) ps.setString(paramIdx++, fechaFin.trim());
                if (estado != null && !estado.isBlank() && !"todos".equalsIgnoreCase(estado)) ps.setString(paramIdx++, estado.trim().toUpperCase());

                try (ResultSet rs = ps.executeQuery()) {
                    int rowNum = 1;
                    while (rs.next()) {
                        SXSSFRow row = sheet.createRow(rowNum++);
                        int c = 0;
                        createCell(row, c++, rs.getInt("idPago"), styleCenter);
                        createCell(row, c++, rs.getString("numeroOperacion"), styleCenter);
                        createCell(row, c++, rs.getString("codigoReserva"), styleCenter);
                        createCell(row, c++, rs.getString("tourNombre"), styleData);
                        createCell(row, c++, String.valueOf(rs.getTimestamp("fechaPago")), styleCenter);
                        createCell(row, c++, rs.getString("metodoPago"), styleCenter);

                        double monto = rs.getDouble("monto");
                        double comision = monto * 0.15;
                        double neto = monto * 0.85;

                        createCurrencyCell(row, c++, monto, styleCurrency);
                        createCurrencyCell(row, c++, comision, styleCurrency);
                        createCurrencyCell(row, c++, neto, styleCurrency);
                        createCell(row, c++, rs.getString("estado"), styleCenter);
                    }
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
        } finally {
            workbook.dispose();
            workbook.close();
        }
    }

    /**
     * 3. ADMIN: Todas las reservas registradas en la plataforma.
     */
    public void generarReservasAdminExcel(String fechaInicio, String fechaFin, String estado, OutputStream out) throws Exception {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        try {
            SXSSFSheet sheet = workbook.createSheet("Reservas Globales");
            sheet.trackAllColumnsForAutoSizing();

            CellStyle styleHeader = createHeaderStyle(workbook, IndexedColors.DARK_BLUE.getIndex());
            CellStyle styleData = createDataStyle(workbook);
            CellStyle styleCurrency = createCurrencyStyle(workbook);
            CellStyle styleCenter = createCenterStyle(workbook);

            String[] headers = {
                "ID Reserva", "Código", "Fecha Reserva", "Agencia Operadora", "Tour", "Fecha Salida",
                "Hora", "Turista", "Email", "Pasajeros", "Total (S/)", "Comisión 15% (S/)", "Estado"
            };

            SXSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                SXSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(styleHeader);
            }

            StringBuilder sql = new StringBuilder("""
                SELECT
                    r.idReserva,
                    r.codigoReserva,
                    r.fechaRegistro,
                    COALESCE(NULLIF(a.nombreComercial, ''), a.razonSocial, 'Agencia') AS agenciaNombre,
                    COALESCE(r.nombreTour, t.nombre, 'Tour Turístico') AS tourNombre,
                    COALESCE(tf.fecha, r.fechaInicio) AS fechaSalida,
                    TIME_FORMAT(tf.horaInicio, '%H:%i') AS horaSalida,
                    COALESCE(CONCAT(p.nombre, ' ', p.apellidoPaterno), u.nombreUsuario, 'Turista') AS turistaNombre,
                    COALESCE(p.email, u.nombreUsuario, '') AS email,
                    (COALESCE(dr.cantAdultos, 1) + COALESCE(dr.cantNinos, 0) + COALESCE(dr.cantBebes, 0)) AS pasajeros,
                    r.total,
                    r.estado
                FROM Reserva r
                LEFT JOIN Agencia a ON r.idAgencia = a.idAgencia
                LEFT JOIN Tour t ON r.idTour = t.idTour
                LEFT JOIN TourFecha tf ON r.idTourFecha = tf.idTourFecha
                LEFT JOIN Usuario u ON r.idUsuario = u.idUsuario
                LEFT JOIN Persona p ON u.idPersona = p.idPersona
                LEFT JOIN DetalleReserva dr ON r.idReserva = dr.idReserva AND (dr.tipo IS NULL OR dr.tipo = 'ADULTO')
                WHERE 1=1
            """);

            if (fechaInicio != null && !fechaInicio.isBlank()) {
                sql.append(" AND DATE(COALESCE(tf.fecha, r.fechaInicio, r.fechaRegistro)) >= ? ");
            }
            if (fechaFin != null && !fechaFin.isBlank()) {
                sql.append(" AND DATE(COALESCE(tf.fecha, r.fechaInicio, r.fechaRegistro)) <= ? ");
            }
            if (estado != null && !estado.isBlank() && !"todos".equalsIgnoreCase(estado)) {
                sql.append(" AND UPPER(r.estado) = ? ");
            }
            sql.append(" ORDER BY r.idReserva DESC");

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql.toString())) {
                int pIdx = 1;
                if (fechaInicio != null && !fechaInicio.isBlank()) ps.setString(pIdx++, fechaInicio.trim());
                if (fechaFin != null && !fechaFin.isBlank()) ps.setString(pIdx++, fechaFin.trim());
                if (estado != null && !estado.isBlank() && !"todos".equalsIgnoreCase(estado)) ps.setString(pIdx++, estado.trim().toUpperCase());

                try (ResultSet rs = ps.executeQuery()) {
                    int rowNum = 1;
                    while (rs.next()) {
                        SXSSFRow row = sheet.createRow(rowNum++);
                        int c = 0;
                        createCell(row, c++, rs.getInt("idReserva"), styleCenter);
                        createCell(row, c++, rs.getString("codigoReserva"), styleCenter);
                        createCell(row, c++, String.valueOf(rs.getTimestamp("fechaRegistro")), styleCenter);
                        createCell(row, c++, rs.getString("agenciaNombre"), styleData);
                        createCell(row, c++, rs.getString("tourNombre"), styleData);
                        createCell(row, c++, String.valueOf(rs.getDate("fechaSalida")), styleCenter);
                        createCell(row, c++, rs.getString("horaSalida") != null ? rs.getString("horaSalida") : "08:00", styleCenter);
                        createCell(row, c++, rs.getString("turistaNombre"), styleData);
                        createCell(row, c++, rs.getString("email"), styleData);
                        createCell(row, c++, rs.getInt("pasajeros"), styleCenter);

                        BigDecimal total = rs.getBigDecimal("total");
                        double valTotal = total != null ? total.doubleValue() : 0.0;
                        createCurrencyCell(row, c++, valTotal, styleCurrency);
                        createCurrencyCell(row, c++, valTotal * 0.15, styleCurrency);
                        createCell(row, c++, rs.getString("estado"), styleCenter);
                    }
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
        } finally {
            workbook.dispose();
            workbook.close();
        }
    }

    /**
     * 4. ADMIN: Liquidación de comisiones por agencia.
     */
    public void generarComisionesAdminExcel(OutputStream out) throws Exception {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        try {
            SXSSFSheet sheet = workbook.createSheet("Comisiones");
            sheet.trackAllColumnsForAutoSizing();

            CellStyle styleHeader = createHeaderStyle(workbook, IndexedColors.INDIGO.getIndex());
            CellStyle styleData = createDataStyle(workbook);
            CellStyle styleCurrency = createCurrencyStyle(workbook);
            CellStyle styleCenter = createCenterStyle(workbook);

            String[] headers = {
                "ID Agencia", "Agencia Afiliada", "RUC", "Periodo", "Ventas Totales (S/)",
                "% Comisión", "Monto Comisión Travelink (S/)", "Monto Neto Agencia (S/)", "Estado"
            };

            SXSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                SXSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(styleHeader);
            }

            String sql = """
                SELECT 
                    a.idAgencia,
                    COALESCE(NULLIF(a.razonSocial, ''), a.nombreComercial, 'Agencia Travelink') AS agencia,
                    COALESCE(a.ruc, '20556677889') AS ruc,
                    'Octubre 2026' AS periodo,
                    COALESCE(SUM(r.total), 0) AS ventasTotales,
                    15.00 AS comisionPct,
                    COALESCE(SUM(r.total * 0.15), 0) AS montoComision,
                    COALESCE(SUM(r.total * 0.85), 0) AS netoAgencia,
                    'Pendiente' AS estado
                FROM Agencia a
                LEFT JOIN Reserva r ON a.idAgencia = r.idAgencia AND UPPER(r.estado) IN ('CONFIRMADA', 'ACTIVA', 'PENDIENTE', 'FINALIZADA')
                WHERE a.estado IN ('ACTIVO', 'Aceptado')
                GROUP BY a.idAgencia, a.razonSocial, a.nombreComercial, a.ruc
                ORDER BY a.idAgencia ASC
            """;

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                int rowNum = 1;
                while (rs.next()) {
                    SXSSFRow row = sheet.createRow(rowNum++);
                    int c = 0;
                    createCell(row, c++, rs.getInt("idAgencia"), styleCenter);
                    createCell(row, c++, rs.getString("agencia"), styleData);
                    createCell(row, c++, rs.getString("ruc"), styleCenter);
                    createCell(row, c++, rs.getString("periodo"), styleCenter);

                    BigDecimal ventas = rs.getBigDecimal("ventasTotales");
                    BigDecimal comision = rs.getBigDecimal("montoComision");
                    BigDecimal neto = rs.getBigDecimal("netoAgencia");

                    createCurrencyCell(row, c++, ventas != null ? ventas.doubleValue() : 0.0, styleCurrency);
                    createCell(row, c++, "15.00%", styleCenter);
                    createCurrencyCell(row, c++, comision != null ? comision.doubleValue() : 0.0, styleCurrency);
                    createCurrencyCell(row, c++, neto != null ? neto.doubleValue() : 0.0, styleCurrency);
                    createCell(row, c++, rs.getString("estado"), styleCenter);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
        } finally {
            workbook.dispose();
            workbook.close();
        }
    }

    /**
     * 5. ADMIN: Listado de usuarios de la plataforma.
     */
    public void generarUsuariosAdminExcel(OutputStream out) throws Exception {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        try {
            SXSSFSheet sheet = workbook.createSheet("Usuarios");
            sheet.trackAllColumnsForAutoSizing();

            CellStyle styleHeader = createHeaderStyle(workbook, IndexedColors.GREY_80_PERCENT.getIndex());
            CellStyle styleData = createDataStyle(workbook);
            CellStyle styleCenter = createCenterStyle(workbook);

            String[] headers = {
                "ID Usuario", "Nombre Completo", "Email", "Rol de Usuario", "Estado", "Fecha Registro"
            };

            SXSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                SXSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(styleHeader);
            }

            String sql = """
                SELECT 
                    u.idUsuario,
                    COALESCE(CONCAT(p.nombre, ' ', p.apellidoPaterno, ' ', COALESCE(p.apellidoMaterno, '')), u.nombreUsuario) AS nombreCompleto,
                    COALESCE(p.email, u.nombreUsuario, '') AS email,
                    r.nombreRol AS rol,
                    u.estado,
                    DATE(u.fechaRegistro) AS fechaRegistro
                FROM Usuario u
                LEFT JOIN Persona p ON u.idPersona = p.idPersona
                JOIN Rol r ON u.idRol = r.idRol
                ORDER BY u.idUsuario DESC
            """;

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                int rowNum = 1;
                while (rs.next()) {
                    SXSSFRow row = sheet.createRow(rowNum++);
                    int c = 0;
                    createCell(row, c++, rs.getInt("idUsuario"), styleCenter);
                    createCell(row, c++, rs.getString("nombreCompleto"), styleData);
                    createCell(row, c++, rs.getString("email"), styleData);
                    createCell(row, c++, rs.getString("rol"), styleCenter);
                    createCell(row, c++, rs.getString("estado"), styleCenter);
                    createCell(row, c++, String.valueOf(rs.getDate("fechaRegistro")), styleCenter);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
        } finally {
            workbook.dispose();
            workbook.close();
        }
    }

    /**
     * 6. ADMIN: Directorio de agencias turísticas afiliadas.
     */
    public void generarAgenciasAdminExcel(OutputStream out) throws Exception {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        try {
            SXSSFSheet sheet = workbook.createSheet("Agencias");
            sheet.trackAllColumnsForAutoSizing();

            CellStyle styleHeader = createHeaderStyle(workbook, IndexedColors.ROYAL_BLUE.getIndex());
            CellStyle styleData = createDataStyle(workbook);
            CellStyle styleCenter = createCenterStyle(workbook);

            String[] headers = {
                "ID Agencia", "Nombre Comercial", "Razón Social", "RUC", "Email", "Teléfono",
                "Estado", "Comisión (%)", "Rating Promedio", "Total Reseñas"
            };

            SXSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                SXSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(styleHeader);
            }

            String sql = """
                SELECT 
                    a.idAgencia,
                    COALESCE(a.nombreComercial, a.razonSocial) AS nombreComercial,
                    a.razonSocial,
                    a.ruc,
                    COALESCE(a.email, '') AS email,
                    COALESCE(a.telefono, '') AS telefono,
                    COALESCE(a.estado, 'ACTIVO') AS estado,
                    COALESCE(a.comision, 15.00) AS comision,
                    COALESCE((SELECT ROUND(AVG(c.estrellas), 1) FROM Calificacion c WHERE c.idAgencia = a.idAgencia AND (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)), 0.0) AS rating,
                    COALESCE((SELECT COUNT(*) FROM Calificacion c WHERE c.idAgencia = a.idAgencia AND (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)), 0) AS totalCalificaciones
                FROM Agencia a
                ORDER BY a.idAgencia ASC
            """;

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                int rowNum = 1;
                while (rs.next()) {
                    SXSSFRow row = sheet.createRow(rowNum++);
                    int c = 0;
                    createCell(row, c++, rs.getInt("idAgencia"), styleCenter);
                    createCell(row, c++, rs.getString("nombreComercial"), styleData);
                    createCell(row, c++, rs.getString("razonSocial"), styleData);
                    createCell(row, c++, rs.getString("ruc"), styleCenter);
                    createCell(row, c++, rs.getString("email"), styleData);
                    createCell(row, c++, rs.getString("telefono"), styleCenter);
                    createCell(row, c++, rs.getString("estado"), styleCenter);
                    createCell(row, c++, rs.getDouble("comision") + "%", styleCenter);
                    createCell(row, c++, rs.getDouble("rating"), styleCenter);
                    createCell(row, c++, rs.getInt("totalCalificaciones"), styleCenter);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
        } finally {
            workbook.dispose();
            workbook.close();
        }
    }

    // =========================================================================
    // MÉTODOS DE APOYO DE ESTILO CELDA Y FORMATO POI
    // =========================================================================

    private CellStyle createHeaderStyle(SXSSFWorkbook wb, short bgColor) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setFillForegroundColor(bgColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private CellStyle createDataStyle(SXSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private CellStyle createCenterStyle(SXSSFWorkbook wb) {
        CellStyle style = createDataStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createCurrencyStyle(SXSSFWorkbook wb) {
        CellStyle style = createDataStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("\"S/ \"#,##0.00"));
        return style;
    }

    private void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
    }

    private void createCell(SXSSFRow row, int col, Object val, CellStyle style) {
        SXSSFCell cell = row.createCell(col);
        if (val instanceof Number) {
            cell.setCellValue(((Number) val).doubleValue());
        } else {
            cell.setCellValue(val != null ? val.toString() : "");
        }
        cell.setCellStyle(style);
    }

    private void createCurrencyCell(SXSSFRow row, int col, double val, CellStyle style) {
        SXSSFCell cell = row.createCell(col);
        cell.setCellValue(val);
        cell.setCellStyle(style);
    }
}
