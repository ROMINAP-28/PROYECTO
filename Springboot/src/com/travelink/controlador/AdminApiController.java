package com.travelink.controlador;

import com.sun.net.httpserver.HttpHandler;
import com.travelink.controlador.admin.*;
import com.travelink.controlador.turista.EliminarResenaHandler;
import com.travelink.controlador.turista.LiquidarComisionHandler;
import com.travelink.server.ServletHttpExchangeAdapter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Controlador REST para el Panel de Administración del Sistema.
 * Gestiona la aprobación/rechazo de agencias, liquidación de comisiones de plataforma (15%),
 * moderación de calidad y reseñas, gestión de usuarios y métricas consolidadas del dashboard.
 */
@RestController
public class AdminApiController {

    private final HttpHandler agenciasHandler = new AgenciasAdminHandler();
    private final HttpHandler notificacionesHandler = new NotificacionesAdminHandler();
    private final HttpHandler destinosHandler = new DestinosAdminHandler();
    private final HttpHandler destinosToursHandler = new DestinosToursAdminHandler();
    private final HttpHandler toursHandler = new ToursAdminHandler();
    private final HttpHandler usuariosHandler = new UsuariosAdminHandler();
    private final HttpHandler reservasHandler = new ReservasAdminHandler();
    private final HttpHandler comisionesHandler = new ComisionesAdminHandler();
    private final HttpHandler liquidarComisionHandler = new LiquidarComisionHandler();
    private final HttpHandler calidadHandler = new CalidadAdminHandler();
    private final HttpHandler eliminarResenaHandler = new EliminarResenaHandler();
    private final HttpHandler dashboardHandler = new DashboardAdminHandler();

    private void delegate(HttpHandler handler, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletHttpExchangeAdapter exchange = new ServletHttpExchangeAdapter(request, response);
        handler.handle(exchange);
        exchange.close();
    }

    @RequestMapping("/api/admin/agencias")
    public void adminAgencias(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(agenciasHandler, req, res);
    }

    @RequestMapping("/api/admin/notificaciones")
    public void adminNotificaciones(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(notificacionesHandler, req, res);
    }

    @RequestMapping("/api/admin/destinos")
    public void adminDestinos(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(destinosHandler, req, res);
    }

    @RequestMapping("/api/admin/destinos/tours")
    public void adminDestinosTours(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(destinosToursHandler, req, res);
    }

    @RequestMapping("/api/admin/tours")
    public void adminTours(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(toursHandler, req, res);
    }

    @RequestMapping({"/api/admin/usuarios", "/api/usuarios", "/api/admin/usuario/guardar", "/api/admin/usuario/eliminar", "/api/usuario/guardar", "/api/usuario/eliminar"})
    public void adminUsuarios(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(usuariosHandler, req, res);
    }

    @RequestMapping({"/api/admin/reservas", "/api/admin/reservas/eliminar", "/api/admin/reserva/eliminar", "/api/reserva/eliminar"})
    public void adminReservas(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(reservasHandler, req, res);
    }

    @RequestMapping({"/api/admin/comisiones", "/api/comisiones"})
    public void adminComisiones(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(comisionesHandler, req, res);
    }

    @RequestMapping({"/api/admin/comision/liquidar", "/api/comision/liquidar", "/api/admin/comisiones/liquidar"})
    public void liquidarComision(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(liquidarComisionHandler, req, res);
    }

    @RequestMapping({"/api/admin/calidad", "/api/calidad"})
    public void adminCalidad(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(calidadHandler, req, res);
    }

    @RequestMapping({"/api/admin/calidad/eliminar", "/api/calidad/eliminar"})
    public void eliminarCalidad(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(eliminarResenaHandler, req, res);
    }

    @RequestMapping("/api/admin/dashboard")
    public void adminDashboard(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(dashboardHandler, req, res);
    }
}
