package com.travelink.reportes;

import com.travelink.config.ConexionDB;
import com.travelink.config.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import com.travelink.servicio.AgenciaServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST para la Generación y Descarga de Reportes y Comprobantes.
 * Coordina la exportación de documentos oficiales en PDF (OpenPDF) y libros Excel (Apache POI)
 * aplicando la nomenclatura estandarizada: reporte_[modulo]_[rol]_[yyyyMMdd].[ext].
 */
@RestController
public class ReportesController {

    private static final Logger log = LoggerFactory.getLogger(ReportesController.class);

    private final PdfReporteService pdfReporteService;
    private final ExcelReporteService excelReporteService;
    private final AgenciaServicio agenciaServicio;

    public ReportesController(PdfReporteService pdfReporteService,
                              ExcelReporteService excelReporteService,
                              AgenciaServicio agenciaServicio) {
        this.pdfReporteService = pdfReporteService;
        this.excelReporteService = excelReporteService;
        this.agenciaServicio = agenciaServicio;
    }

    // =========================================================================
    // 1. PDF COMPROBANTE DE RESERVA
    // GET /api/turista/reservas/{id}/comprobante.pdf
    // =========================================================================
    @GetMapping({
        "/api/turista/reservas/{id}/comprobante.pdf",
        "/api/turista/reservas/comprobante.pdf",
        "/api/reservas/{id}/comprobante.pdf"
    })
    public void descargarComprobantePdf(
            @PathVariable(required = false) String id,
            @RequestParam(required = false) String codigo,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        Optional<Integer> optUserId = SecurityUtils.getAuthenticatedIdUsuario();
        if (optUserId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":\"error\",\"message\":\"No autenticado\"}");
            return;
        }

        int authUserId = optUserId.get();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"));
        boolean isAgencia = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("AGENCIA"));

        // Resolver idReserva o código
        int idReserva = 0;
        String cod = codigo != null ? codigo.trim() : "";
        if (id != null && !id.isBlank()) {
            try {
                idReserva = Integer.parseInt(id.trim());
            } catch (NumberFormatException e) {
                cod = id.trim();
            }
        }

        // Consultar propiedad de la reserva
        int resIdUsuario = 0;
        int resIdAgencia = 0;
        String resCodigo = "";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT idReserva, idUsuario, idAgencia, codigoReserva FROM Reserva WHERE (idReserva = ? AND ? > 0) OR (codigoReserva = ? AND ? != '') LIMIT 1")) {
            ps.setInt(1, idReserva);
            ps.setInt(2, idReserva);
            ps.setString(3, cod);
            ps.setString(4, cod);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idReserva = rs.getInt("idReserva");
                    resIdUsuario = rs.getInt("idUsuario");
                    resIdAgencia = rs.getInt("idAgencia");
                    resCodigo = rs.getString("codigoReserva");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"status\":\"error\",\"message\":\"Reserva no encontrada\"}");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // Validar permisos: Turista dueño, Agencia dueña del tour o Admin
        if (!isAdmin) {
            if (isAgencia) {
                int agencyId = resolverIdAgencia(request, authUserId);
                if (agencyId != resIdAgencia) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"status\":\"error\",\"message\":\"Acceso denegado: este comprobante pertenece a otra agencia\"}");
                    return;
                }
            } else {
                if (authUserId != resIdUsuario) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"status\":\"error\",\"message\":\"Acceso denegado: este comprobante pertenece a otro usuario\"}");
                    return;
                }
            }
        }

        // Enviar PDF
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + generarNombreReporte("reserva", "turista", "pdf") + "\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        boolean ok = pdfReporteService.generarComprobanteReservaPdf(idReserva, resCodigo, response.getOutputStream());
        if (!ok) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================================
    // 2. PDF COMISIONES (ADMIN)
    // GET /api/admin/reportes/comisiones.pdf
    // =========================================================================
    @GetMapping({"/api/admin/reportes/comisiones.pdf", "/api/admin/reportes/comisiones-pdf"})
    public void descargarComisionesPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + generarNombreReporte("comisiones", "admin", "pdf") + "\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        boolean ok = pdfReporteService.generarReporteComisionesPdf(response.getOutputStream());
        if (!ok) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================================
    // 3. EXCEL AGENCIA: RESERVAS
    // GET /api/agencia/reportes/reservas.xlsx
    // =========================================================================
    @GetMapping({"/api/agencia/reportes/reservas.xlsx", "/api/agencia/reportes/reservas"})
    public void descargarReservasAgenciaExcel(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String estado,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        Optional<Integer> optUserId = SecurityUtils.getAuthenticatedIdUsuario();
        int agencyId = resolverIdAgencia(request, optUserId.orElse(0));

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + generarNombreReporte("reservas", "agencia", "xlsx") + "\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        excelReporteService.generarReservasAgenciaExcel(agencyId, fechaInicio, fechaFin, estado, response.getOutputStream());
    }

    // =========================================================================
    // 4. EXCEL AGENCIA: PAGOS
    // GET /api/agencia/reportes/pagos.xlsx
    // =========================================================================
    @GetMapping({"/api/agencia/reportes/pagos.xlsx", "/api/agencia/reportes/pagos"})
    public void descargarPagosAgenciaExcel(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String estado,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        Optional<Integer> optUserId = SecurityUtils.getAuthenticatedIdUsuario();
        int agencyId = resolverIdAgencia(request, optUserId.orElse(0));

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + generarNombreReporte("pagos", "agencia", "xlsx") + "\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        excelReporteService.generarPagosAgenciaExcel(agencyId, fechaInicio, fechaFin, estado, response.getOutputStream());
    }

    // =========================================================================
    // 5. EXCEL ADMIN: RESERVAS GLOBALES
    // GET /api/admin/reportes/reservas.xlsx
    // =========================================================================
    @GetMapping({"/api/admin/reportes/reservas.xlsx", "/api/admin/reportes/reservas"})
    public void descargarReservasAdminExcel(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String estado,
            HttpServletResponse response) throws Exception {

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + generarNombreReporte("reservas", "admin", "xlsx") + "\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        excelReporteService.generarReservasAdminExcel(fechaInicio, fechaFin, estado, response.getOutputStream());
    }

    // =========================================================================
    // 6. EXCEL ADMIN: COMISIONES
    // GET /api/admin/reportes/comisiones.xlsx
    // =========================================================================
    @GetMapping({"/api/admin/reportes/comisiones.xlsx", "/api/admin/reportes/comisiones"})
    public void descargarComisionesAdminExcel(HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + generarNombreReporte("comisiones", "admin", "xlsx") + "\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        excelReporteService.generarComisionesAdminExcel(response.getOutputStream());
    }

    // =========================================================================
    // 7. EXCEL ADMIN: USUARIOS
    // GET /api/admin/reportes/usuarios.xlsx
    // =========================================================================
    @GetMapping({"/api/admin/reportes/usuarios.xlsx", "/api/admin/reportes/usuarios"})
    public void descargarUsuariosAdminExcel(HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + generarNombreReporte("usuarios", "admin", "xlsx") + "\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        excelReporteService.generarUsuariosAdminExcel(response.getOutputStream());
    }

    // =========================================================================
    // 8. EXCEL ADMIN: AGENCIAS
    // GET /api/admin/reportes/agencias.xlsx
    // =========================================================================
    @GetMapping({"/api/admin/reportes/agencias.xlsx", "/api/admin/reportes/agencias"})
    public void descargarAgenciasAdminExcel(HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + generarNombreReporte("agencias", "admin", "xlsx") + "\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        excelReporteService.generarAgenciasAdminExcel(response.getOutputStream());
    }

    private String generarNombreReporte(String modulo, String rol, String extension) {
        String fecha = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return String.format("reporte_%s_%s_%s.%s", modulo.toLowerCase(), rol.toLowerCase(), fecha, extension.toLowerCase());
    }

    private int resolverIdAgencia(HttpServletRequest request, int userId) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object sId = session.getAttribute("ID_AGENCIA");
            if (sId instanceof Integer && (Integer) sId > 0) return (Integer) sId;
        }
        if (userId > 0) {
            Optional<com.travelink.entidades.Agencia> ag = agenciaServicio.buscarPorIdUsuario(userId);
            if (ag.isPresent()) return ag.get().getIdAgencia();
        }
        return 1;
    }
}
