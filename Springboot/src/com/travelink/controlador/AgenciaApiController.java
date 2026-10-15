package com.travelink.controlador;

import com.sun.net.httpserver.HttpHandler;
import com.travelink.controlador.agencia.*;
import com.travelink.server.ServletHttpExchangeAdapter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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

    private final com.travelink.repositorio.AgenciaRepositorio agenciaRepositorio;

    public AgenciaApiController(com.travelink.repositorio.AgenciaRepositorio agenciaRepositorio) {
        this.agenciaRepositorio = agenciaRepositorio;
    }

    private void delegate(HttpHandler handler, HttpServletRequest request, HttpServletResponse response) throws IOException {
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        Integer sessionAgenciaId = session != null ? (Integer) session.getAttribute("ID_AGENCIA") : null;

        if (sessionAgenciaId == null) {
            java.util.Optional<Integer> optUser = com.travelink.config.SecurityUtils.getAuthenticatedIdUsuario();
            if (optUser.isPresent()) {
                java.util.Optional<com.travelink.entidades.Agencia> ag = agenciaRepositorio.findByIdUsuario(optUser.get());
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
}
