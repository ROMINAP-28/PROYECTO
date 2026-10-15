package com.travelink.reportes;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.travelink.config.ConexionDB;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReporteService {

    // Paleta corporativa Travelink
    private static final Color COLOR_PRIMARY = new Color(11, 31, 56);     // #0B1F38 Navy profundo
    private static final Color COLOR_ACCENT = new Color(25, 111, 61);     // #196F3D Verde esmeralda
    private static final Color COLOR_GOLD = new Color(217, 119, 6);       // #D97706 Dorado cálido
    private static final Color COLOR_BG_LIGHT = new Color(248, 250, 252); // #F8FAFC
    private static final Color COLOR_BORDER = new Color(226, 232, 240);   // #E2E8F0
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);// #64748B

    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COLOR_PRIMARY);
    private static final Font FONT_SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT_MUTED);
    private static final Font FONT_HEADER_WHITE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
    private static final Font FONT_SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARY);
    private static final Font FONT_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, COLOR_TEXT_MUTED);
    private static final Font FONT_VALUE = FontFactory.getFont(FontFactory.HELVETICA, 9.5f, Color.BLACK);
    private static final Font FONT_TOTAL_BIG = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, COLOR_ACCENT);

    /**
     * Genera comprobante electrónico de reserva en formato PDF con OpenPDF.
     */
    public boolean generarComprobanteReservaPdf(int idReserva, String codigoReserva, OutputStream out) {
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Consulta de datos de la reserva
            String sql = """
                SELECT
                    r.idReserva,
                    r.codigoReserva,
                    r.idUsuario,
                    r.idAgencia,
                    r.fechaRegistro,
                    r.fechaInicio,
                    r.fechaFin,
                    r.total,
                    r.estado AS estadoReserva,
                    COALESCE(r.nombreTour, t.nombre, 'Tour Turístico') AS tourNombre,
                    COALESCE(t.categoria, 'Cultura') AS categoria,
                    COALESCE(t.duracion, '1 día') AS duracion,
                    COALESCE(t.ubicacion, d.nombre, 'Perú') AS ubicacion,
                    t.precioAdulto,
                    t.precioNino,
                    t.precioBebe,
                    t.descuento,
                    COALESCE(NULLIF(a.nombreComercial, ''), a.razonSocial, 'Agencia Afiliada Travelink') AS agenciaNombre,
                    COALESCE(a.razonSocial, a.nombreComercial, '') AS agenciaRazonSocial,
                    COALESCE(a.ruc, '20556677889') AS agenciaRuc,
                    COALESCE(a.email, 'contacto@travelink.pe') AS agenciaEmail,
                    COALESCE(a.telefono, '+51 984 123 456') AS agenciaTelefono,
                    COALESCE(d.nombre, 'Destino Turístico') AS destinoNombre,
                    COALESCE(tf.fecha, r.fechaInicio) AS fechaSalida,
                    TIME_FORMAT(tf.horaInicio, '%H:%i') AS horaSalida,
                    COALESCE(CONCAT(p.nombre, ' ', p.apellidoPaterno, ' ', COALESCE(p.apellidoMaterno, '')), u.nombreUsuario, 'Turista') AS titularNombre,
                    COALESCE(p.email, u.nombreUsuario, 'correo@travelink.test') AS titularEmail,
                    COALESCE(p.telefono, '+51 999 888 777') AS titularTelefono,
                    COALESCE(p.nroDocumento, '72345678') AS titularDoc,
                    COALESCE(dr.cantAdultos, 1) AS cantAdultos,
                    COALESCE(dr.cantNinos, 0) AS cantNinos,
                    COALESCE(dr.cantBebes, 0) AS cantBebes,
                    COALESCE(
                        (SELECT pg.metodoPago FROM Pago pg WHERE pg.idReserva = r.idReserva ORDER BY pg.idPago DESC LIMIT 1),
                        (SELECT mp.tipoPago FROM MetodoPago mp WHERE mp.idReserva = r.idReserva ORDER BY mp.idMetodoPago DESC LIMIT 1),
                        'TARJETA'
                    ) AS metodoPago,
                    COALESCE(
                        (SELECT pg.numeroOperacion FROM Pago pg WHERE pg.idReserva = r.idReserva ORDER BY pg.idPago DESC LIMIT 1),
                        (SELECT mp.numeroOperacion FROM MetodoPago mp WHERE mp.idReserva = r.idReserva ORDER BY mp.idMetodoPago DESC LIMIT 1),
                        CONCAT('OP-', r.codigoReserva)
                    ) AS numeroOperacion,
                    COALESCE(
                        (SELECT pg.estado FROM Pago pg WHERE pg.idReserva = r.idReserva ORDER BY pg.idPago DESC LIMIT 1),
                        (SELECT mp.estadoPago FROM MetodoPago mp WHERE mp.idReserva = r.idReserva ORDER BY mp.idMetodoPago DESC LIMIT 1),
                        'COMPLETADO'
                    ) AS estadoPago
                FROM Reserva r
                LEFT JOIN Tour t ON r.idTour = t.idTour
                LEFT JOIN TourFecha tf ON r.idTourFecha = tf.idTourFecha
                LEFT JOIN Agencia a ON r.idAgencia = a.idAgencia
                LEFT JOIN Destino d ON t.idDestino = d.idDestino
                LEFT JOIN Usuario u ON r.idUsuario = u.idUsuario
                LEFT JOIN Persona p ON u.idPersona = p.idPersona
                LEFT JOIN DetalleReserva dr ON r.idReserva = dr.idReserva AND (dr.tipo IS NULL OR dr.tipo = 'ADULTO')
                WHERE (r.idReserva = ? AND ? > 0) OR (r.codigoReserva = ? AND ? != '')
                LIMIT 1
            """;

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idReserva);
                ps.setInt(2, idReserva);
                ps.setString(3, codigoReserva != null ? codigoReserva : "");
                ps.setString(4, codigoReserva != null ? codigoReserva : "");

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }

                    // Encabezado corporativo
                    PdfPTable headTable = new PdfPTable(2);
                    headTable.setWidthPercentage(100);
                    headTable.setWidths(new float[]{60f, 40f});

                    PdfPCell brandCell = new PdfPCell();
                    brandCell.setBorder(Rectangle.NO_BORDER);
                    Paragraph pBrand = new Paragraph("TRAVELINK", FONT_TITLE);
                    Paragraph pSlogan = new Paragraph("Plataforma Intermediaria de Turismo en el Perú", FONT_SUBTITLE);
                    brandCell.addElement(pBrand);
                    brandCell.addElement(pSlogan);
                    headTable.addCell(brandCell);

                    PdfPCell receiptCell = new PdfPCell();
                    receiptCell.setBorder(Rectangle.BOX);
                    receiptCell.setBorderColor(COLOR_ACCENT);
                    receiptCell.setBorderWidth(1.5f);
                    receiptCell.setBackgroundColor(COLOR_BG_LIGHT);
                    receiptCell.setPadding(8f);
                    receiptCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                    Paragraph pVoucher = new Paragraph("COMPROBANTE DE RESERVA", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_ACCENT));
                    pVoucher.setAlignment(Element.ALIGN_CENTER);
                    Paragraph pCodigo = new Paragraph(rs.getString("codigoReserva"), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY));
                    pCodigo.setAlignment(Element.ALIGN_CENTER);
                    Paragraph pFechaEmision = new Paragraph("Emisión: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), FontFactory.getFont(FontFactory.HELVETICA, 7.5f, COLOR_TEXT_MUTED));
                    pFechaEmision.setAlignment(Element.ALIGN_CENTER);

                    receiptCell.addElement(pVoucher);
                    receiptCell.addElement(pCodigo);
                    receiptCell.addElement(pFechaEmision);
                    headTable.addCell(receiptCell);

                    document.add(headTable);
                    document.add(new Paragraph("\n"));

                    // Información en dos columnas: Agencia & Titular
                    PdfPTable infoGrid = new PdfPTable(2);
                    infoGrid.setWidthPercentage(100);
                    infoGrid.setWidths(new float[]{50f, 50f});

                    // Columna 1: Agencia
                    PdfPCell cAgencia = new PdfPCell();
                    cAgencia.setBorder(Rectangle.BOX);
                    cAgencia.setBorderColor(COLOR_BORDER);
                    cAgencia.setBackgroundColor(COLOR_BG_LIGHT);
                    cAgencia.setPadding(8f);
                    cAgencia.addElement(new Paragraph("AGENCIA OPERADORA", FONT_SECTION));
                    cAgencia.addElement(new Paragraph("Nombre Comercial: " + rs.getString("agenciaNombre"), FONT_VALUE));
                    cAgencia.addElement(new Paragraph("Razón Social: " + rs.getString("agenciaRazonSocial"), FONT_VALUE));
                    cAgencia.addElement(new Paragraph("RUC: " + rs.getString("agenciaRuc"), FONT_VALUE));
                    cAgencia.addElement(new Paragraph("Contacto: " + rs.getString("agenciaTelefono") + " | " + rs.getString("agenciaEmail"), FONT_VALUE));
                    infoGrid.addCell(cAgencia);

                    // Columna 2: Titular
                    PdfPCell cTitular = new PdfPCell();
                    cTitular.setBorder(Rectangle.BOX);
                    cTitular.setBorderColor(COLOR_BORDER);
                    cTitular.setBackgroundColor(COLOR_BG_LIGHT);
                    cTitular.setPadding(8f);
                    cTitular.addElement(new Paragraph("DATOS DEL TITULAR", FONT_SECTION));
                    cTitular.addElement(new Paragraph("Nombre: " + rs.getString("titularNombre"), FONT_VALUE));
                    cTitular.addElement(new Paragraph("N° Documento: " + rs.getString("titularDoc"), FONT_VALUE));
                    cTitular.addElement(new Paragraph("Email: " + rs.getString("titularEmail"), FONT_VALUE));
                    cTitular.addElement(new Paragraph("Teléfono: " + rs.getString("titularTelefono"), FONT_VALUE));
                    infoGrid.addCell(cTitular);

                    document.add(infoGrid);
                    document.add(new Paragraph("\n"));

                    // Información de la salida turística
                    PdfPTable tourBox = new PdfPTable(4);
                    tourBox.setWidthPercentage(100);
                    tourBox.setWidths(new float[]{35f, 25f, 20f, 20f});

                    PdfPCell hTour = new PdfPCell(new Phrase("DETALLES DE LA EXPERIENCIA", FONT_HEADER_WHITE));
                    hTour.setColspan(4);
                    hTour.setBackgroundColor(COLOR_PRIMARY);
                    hTour.setPadding(6f);
                    tourBox.addCell(hTour);

                    addCellItem(tourBox, "Experiencia / Tour:", rs.getString("tourNombre"));
                    addCellItem(tourBox, "Destino / Ubicación:", rs.getString("ubicacion"));
                    addCellItem(tourBox, "Fecha Salida:", String.valueOf(rs.getDate("fechaSalida")));
                    addCellItem(tourBox, "Hora Salida:", rs.getString("horaSalida") != null ? rs.getString("horaSalida") : "08:00 AM");

                    document.add(tourBox);
                    document.add(new Paragraph("\n"));

                    // Desglose de Pasajeros y Precios
                    int cAdultos = rs.getInt("cantAdultos");
                    int cNinos = rs.getInt("cantNinos");
                    int cBebes = rs.getInt("cantBebes");
                    BigDecimal pAdulto = rs.getBigDecimal("precioAdulto") != null ? rs.getBigDecimal("precioAdulto") : BigDecimal.ZERO;
                    BigDecimal pNino = rs.getBigDecimal("precioNino") != null ? rs.getBigDecimal("precioNino") : BigDecimal.ZERO;
                    BigDecimal pBebe = rs.getBigDecimal("precioBebe") != null ? rs.getBigDecimal("precioBebe") : BigDecimal.ZERO;
                    BigDecimal totalReserva = rs.getBigDecimal("total") != null ? rs.getBigDecimal("total") : BigDecimal.ZERO;

                    PdfPTable priceTable = new PdfPTable(4);
                    priceTable.setWidthPercentage(100);
                    priceTable.setWidths(new float[]{45f, 15f, 20f, 20f});

                    addTableHeader(priceTable, "Descripción / Pasajeros", "Cantidad", "Precio Unitario", "Subtotal");

                    if (cAdultos > 0) {
                        BigDecimal subAdultos = pAdulto.multiply(BigDecimal.valueOf(cAdultos));
                        addTableRow(priceTable, "Pasajero(s) Adulto", String.valueOf(cAdultos), "S/ " + pAdulto.setScale(2), "S/ " + subAdultos.setScale(2));
                    }
                    if (cNinos > 0) {
                        BigDecimal subNinos = pNino.multiply(BigDecimal.valueOf(cNinos));
                        addTableRow(priceTable, "Pasajero(s) Niño", String.valueOf(cNinos), "S/ " + pNino.setScale(2), "S/ " + subNinos.setScale(2));
                    }
                    if (cBebes > 0) {
                        BigDecimal subBebes = pBebe.multiply(BigDecimal.valueOf(cBebes));
                        addTableRow(priceTable, "Pasajero(s) Bebé", String.valueOf(cBebes), "S/ " + pBebe.setScale(2), "S/ " + subBebes.setScale(2));
                    }

                    // Total
                    PdfPCell cellTotalLabel = new PdfPCell(new Phrase("TOTAL PAGADO (PEN):", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_PRIMARY)));
                    cellTotalLabel.setColspan(3);
                    cellTotalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cellTotalLabel.setPadding(8f);
                    cellTotalLabel.setBackgroundColor(COLOR_BG_LIGHT);
                    priceTable.addCell(cellTotalLabel);

                    PdfPCell cellTotalVal = new PdfPCell(new Phrase("S/ " + totalReserva.setScale(2), FONT_TOTAL_BIG));
                    cellTotalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cellTotalVal.setPadding(8f);
                    cellTotalVal.setBackgroundColor(COLOR_BG_LIGHT);
                    priceTable.addCell(cellTotalVal);

                    document.add(priceTable);
                    document.add(new Paragraph("\n"));

                    // Información de Pago y Estado
                    PdfPTable payTable = new PdfPTable(3);
                    payTable.setWidthPercentage(100);
                    payTable.setWidths(new float[]{34f, 33f, 33f});

                    PdfPCell hPay = new PdfPCell(new Phrase("INFORMACIÓN DEL PAGO Y TRANSACCIÓN", FONT_HEADER_WHITE));
                    hPay.setColspan(3);
                    hPay.setBackgroundColor(COLOR_PRIMARY);
                    hPay.setPadding(6f);
                    payTable.addCell(hPay);

                    addCellItem(payTable, "Método de Pago:", rs.getString("metodoPago"));
                    addCellItem(payTable, "N° de Operación:", rs.getString("numeroOperacion"));
                    addCellItem(payTable, "Estado de Reserva:", rs.getString("estadoReserva") + " (" + rs.getString("estadoPago") + ")");

                    document.add(payTable);
                    document.add(new Paragraph("\n"));

                    // Footer / Notas importantes
                    Paragraph pNoticeTitle = new Paragraph("INSTRUCCIONES IMPORTANTES PARA EL VIAJERO:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, COLOR_PRIMARY));
                    Paragraph pNotice1 = new Paragraph("1. Presenta este comprobante (físico o digital) junto con tu documento de identidad en el punto de encuentro.", FontFactory.getFont(FontFactory.HELVETICA, 8f, COLOR_TEXT_MUTED));
                    Paragraph pNotice2 = new Paragraph("2. Por favor presentarse 15 minutos antes de la hora fijada para la salida de la experiencia turística.", FontFactory.getFont(FontFactory.HELVETICA, 8f, COLOR_TEXT_MUTED));
                    Paragraph pNotice3 = new Paragraph("3. Para dudas sobre logística o equipaje, contacta directamente a la agencia operadora.", FontFactory.getFont(FontFactory.HELVETICA, 8f, COLOR_TEXT_MUTED));
                    Paragraph pThanks = new Paragraph("\n¡Gracias por elegir Travelink! Tu portal confiable de turismo en el Perú.", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, COLOR_ACCENT));
                    pThanks.setAlignment(Element.ALIGN_CENTER);

                    document.add(pNoticeTitle);
                    document.add(pNotice1);
                    document.add(pNotice2);
                    document.add(pNotice3);
                    document.add(pThanks);
                }
            }

            document.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Genera reporte de comisiones de la plataforma para el Administrador en formato PDF con OpenPDF.
     */
    public boolean generarReporteComisionesPdf(OutputStream out) {
        Document document = new Document(PageSize.A4.rotate(), 30, 30, 35, 35);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Encabezado
            Paragraph pTitle = new Paragraph("TRAVELINK - REPORTE GENERAL DE COMISIONES", FONT_TITLE);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            Paragraph pSub = new Paragraph("Liquidación Financiera y Rentabilidad de Plataforma - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), FONT_SUBTITLE);
            pSub.setAlignment(Element.ALIGN_CENTER);

            document.add(pTitle);
            document.add(pSub);
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{6f, 26f, 16f, 13f, 13f, 13f, 13f});

            addTableHeader(table, "#", "Agencia Afiliada", "RUC", "Ventas Totales", "Comisión (15%)", "Neto Agencia", "Estado");

            String sql = """
                SELECT 
                    a.idAgencia,
                    COALESCE(NULLIF(a.razonSocial, ''), a.nombreComercial, 'Agencia Travelink') AS agencia,
                    COALESCE(a.ruc, '20556677889') AS ruc,
                    COALESCE(SUM(r.total), 0) AS ventasTotales,
                    COALESCE(SUM(r.total * 0.15), 0) AS montoComision,
                    COALESCE(SUM(r.total * 0.85), 0) AS netoAgencia,
                    'Pendiente' AS estado
                FROM Agencia a
                LEFT JOIN Reserva r ON a.idAgencia = r.idAgencia AND UPPER(r.estado) IN ('CONFIRMADA', 'ACTIVA', 'PENDIENTE', 'FINALIZADA')
                WHERE a.estado IN ('ACTIVO', 'Aceptado')
                GROUP BY a.idAgencia, a.razonSocial, a.nombreComercial, a.ruc
                ORDER BY a.idAgencia ASC
            """;

            BigDecimal totalVentas = BigDecimal.ZERO;
            BigDecimal totalComisiones = BigDecimal.ZERO;
            BigDecimal totalNeto = BigDecimal.ZERO;

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                int idx = 1;
                while (rs.next()) {
                    BigDecimal ventas = rs.getBigDecimal("ventasTotales");
                    BigDecimal comision = rs.getBigDecimal("montoComision");
                    BigDecimal neto = rs.getBigDecimal("netoAgencia");

                    totalVentas = totalVentas.add(ventas);
                    totalComisiones = totalComisiones.add(comision);
                    totalNeto = totalNeto.add(neto);

                    addTableRow(table,
                            String.valueOf(idx++),
                            rs.getString("agencia"),
                            rs.getString("ruc"),
                            "S/ " + ventas.setScale(2),
                            "S/ " + comision.setScale(2),
                            "S/ " + neto.setScale(2),
                            rs.getString("estado")
                    );
                }
            }

            // Fila de totales
            PdfPCell cellTotal = new PdfPCell(new Phrase("TOTALES CONSOLIDADOS:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_PRIMARY)));
            cellTotal.setColspan(3);
            cellTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotal.setBackgroundColor(COLOR_BG_LIGHT);
            cellTotal.setPadding(6f);
            table.addCell(cellTotal);

            PdfPCell cVentas = new PdfPCell(new Phrase("S/ " + totalVentas.setScale(2), FONT_SECTION));
            cVentas.setBackgroundColor(COLOR_BG_LIGHT);
            cVentas.setPadding(6f);
            table.addCell(cVentas);

            PdfPCell cComision = new PdfPCell(new Phrase("S/ " + totalComisiones.setScale(2), FONT_TOTAL_BIG));
            cComision.setBackgroundColor(COLOR_BG_LIGHT);
            cComision.setPadding(6f);
            table.addCell(cComision);

            PdfPCell cNeto = new PdfPCell(new Phrase("S/ " + totalNeto.setScale(2), FONT_SECTION));
            cNeto.setBackgroundColor(COLOR_BG_LIGHT);
            cNeto.setPadding(6f);
            table.addCell(cNeto);

            PdfPCell cBlank = new PdfPCell(new Phrase("-", FONT_VALUE));
            cBlank.setBackgroundColor(COLOR_BG_LIGHT);
            cBlank.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cBlank);

            document.add(table);
            document.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FONT_HEADER_WHITE));
            cell.setBackgroundColor(COLOR_PRIMARY);
            cell.setPadding(6f);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, String... values) {
        for (String v : values) {
            PdfPCell cell = new PdfPCell(new Phrase(v != null ? v : "", FONT_VALUE));
            cell.setPadding(5f);
            table.addCell(cell);
        }
    }

    private void addCellItem(PdfPTable table, String label, String val) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.BOX);
        c.setBorderColor(COLOR_BORDER);
        c.setBackgroundColor(Color.WHITE);
        c.setPadding(5f);
        c.addElement(new Paragraph(label, FONT_LABEL));
        c.addElement(new Paragraph(val != null ? val : "-", FONT_VALUE));
        table.addCell(c);
    }
}
