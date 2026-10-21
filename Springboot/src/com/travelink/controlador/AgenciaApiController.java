package com.travelink.controlador;

import com.sun.net.httpserver.HttpHandler;
import com.travelink.controlador.agencia.*;
import com.travelink.server.ServletHttpExchangeAdapter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Controlador REST para el Portal de Agencias de Viaje.
 * Expone endpoints de gestión de paquetes turísticos, disponibilidad de fechas,
 * importación masiva desde Excel/CSV, consulta de reservas y reportes operacionales.
 */
@RestController
@RequestMapping("/api/agencia")
public class AgenciaApiController {

    private final HttpHandler registroHandler = new RegistroAgenciaHandler();
    private final HttpHandler destinosHandler = new DestinosAgenciaHandler();
    private final HttpHandler tiposHandler = new TiposAgenciaHandler();
    private final HttpHandler serviciosHandler = new ServiciosAgenciaHandler();
    private final HttpHandler disponibilidadHandler = new DisponibilidadAgenciaHandler();
    private final HttpHandler paquetesHandler = new PaquetesAgenciaHandler();
    private final HttpHandler reservasHandler = new ReservasAgenciaHandler();
    private final HttpHandler pagosHandler = new PagosAgenciaHandler();
    private final HttpHandler dashboardHandler = new DashboardAgenciaHandler();

    private final com.travelink.servicio.AgenciaServicio agenciaServicio;
    private final com.travelink.servicio.ImportacionServicio importacionServicio;

    public AgenciaApiController(com.travelink.servicio.AgenciaServicio agenciaServicio,
                                com.travelink.servicio.ImportacionServicio importacionServicio) {
        this.agenciaServicio = agenciaServicio;
        this.importacionServicio = importacionServicio;
    }

    private void delegate(HttpHandler handler, HttpServletRequest request, HttpServletResponse response) throws IOException {
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        Integer sessionAgenciaId = session != null ? (Integer) session.getAttribute("ID_AGENCIA") : null;

        if (sessionAgenciaId == null) {
            java.util.Optional<Integer> optUser = com.travelink.config.SecurityUtils.getAuthenticatedIdUsuario();
            if (optUser.isPresent()) {
                java.util.Optional<com.travelink.entidades.Agencia> ag = agenciaServicio.buscarPorIdUsuario(optUser.get());
                if (ag.isPresent()) {
                    sessionAgenciaId = ag.get().getIdAgencia();
                    if (session != null) {
                        session.setAttribute("ID_AGENCIA", sessionAgenciaId);
                    }
                }
            }
        }

        ServletHttpExchangeAdapter exchange = new ServletHttpExchangeAdapter(request, response);
        if (sessionAgenciaId != null) {
            exchange.setAttribute("ID_AGENCIA", sessionAgenciaId);
            request.setAttribute("ID_AGENCIA", sessionAgenciaId);
        }
        handler.handle(exchange);
        exchange.close();
    }

    @RequestMapping("/registro")
    public void registro(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(registroHandler, req, res);
    }

    @RequestMapping("/destinos")
    public void destinos(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(destinosHandler, req, res);
    }

    @RequestMapping("/tipos")
    public void tipos(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(tiposHandler, req, res);
    }

    @RequestMapping("/servicios")
    public void servicios(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(serviciosHandler, req, res);
    }

    @RequestMapping("/disponibilidad")
    public void disponibilidad(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(disponibilidadHandler, req, res);
    }

    @RequestMapping("/paquetes")
    public void paquetes(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(paquetesHandler, req, res);
    }

    @RequestMapping("/reservas")
    public void reservas(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(reservasHandler, req, res);
    }

    @RequestMapping("/pagos")
    public void pagos(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(pagosHandler, req, res);
    }

    @RequestMapping("/dashboard")
    public void dashboard(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(dashboardHandler, req, res);
    }

    @org.springframework.web.bind.annotation.PostMapping("/importar/disponibilidad")
    public org.springframework.http.ResponseEntity<?> importarDisponibilidad(
            @org.springframework.web.bind.annotation.RequestParam(value = "archivo", required = false) org.springframework.web.multipart.MultipartFile archivo,
            @org.springframework.web.bind.annotation.RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            HttpServletRequest request) {

        org.springframework.web.multipart.MultipartFile uploadFile = archivo != null ? archivo : file;
        if (uploadFile == null || uploadFile.isEmpty()) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(java.util.Map.of("status", "error", "message", "Debe seleccionar un archivo Excel (.xlsx) o CSV (.csv)"));
        }

        int idAgencia = resolverIdAgencia(request);
        com.travelink.controlador.dto.ImportacionResultadoDto resultado = importacionServicio.importarDisponibilidad(uploadFile, idAgencia);
        return org.springframework.http.ResponseEntity.ok(resultado);
    }

    @org.springframework.web.bind.annotation.GetMapping("/plantilla/disponibilidad.xlsx")
    public void descargarPlantilla(HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"plantilla_disponibilidad_tours.xlsx\"");
        importacionServicio.generarPlantillaDisponibilidad(response.getOutputStream());
    }

    private int resolverIdAgencia(HttpServletRequest request) {
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            Object sId = session.getAttribute("ID_AGENCIA");
            if (sId instanceof Integer && (Integer) sId > 0) return (Integer) sId;
        }
        java.util.Optional<Integer> optUser = com.travelink.config.SecurityUtils.getAuthenticatedIdUsuario();
        if (optUser.isPresent()) {
            java.util.Optional<com.travelink.entidades.Agencia> ag = agenciaServicio.buscarPorIdUsuario(optUser.get());
            if (ag.isPresent()) return ag.get().getIdAgencia();
        }
        return 1;
    }
}
