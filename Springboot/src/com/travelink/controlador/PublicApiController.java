package com.travelink.controlador;

import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.SecurityUtils;
import com.travelink.controlador.admin.DestinosAdminHandler;
import com.travelink.controlador.admin.DestinosToursAdminHandler;
import com.travelink.controlador.admin.ToursAdminHandler;
import com.travelink.controlador.turista.*;
import com.travelink.server.ServletHttpExchangeAdapter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;

@RestController
public class PublicApiController {

    private final HttpHandler registroHandler = new RegistroHandler();
    private final HttpHandler guardarReservaHandler = new GuardarReservaHandler();
    private final HttpHandler obtenerReservasHandler = new ObtenerReservasHandler();
    private final HttpHandler cancelarReservaHandler = new CancelarReservaHandler();
    private final HttpHandler calificarHandler = new CalificarHandler();
    private final HttpHandler destinosHandler = new DestinosAdminHandler();
    private final HttpHandler destinosToursHandler = new DestinosToursAdminHandler();
    private final HttpHandler toursHandler = new ToursAdminHandler();

    private void delegate(HttpHandler handler, HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Integer sessionUserId = session != null ? (Integer) session.getAttribute("ID_USUARIO") : null;
        if (sessionUserId == null) {
            Optional<Integer> opt = SecurityUtils.getAuthenticatedIdUsuario();
            if (opt.isPresent()) {
                sessionUserId = opt.get();
                if (session != null) session.setAttribute("ID_USUARIO", sessionUserId);
            }
        }

        ServletHttpExchangeAdapter exchange = new ServletHttpExchangeAdapter(request, response);
        if (sessionUserId != null) {
            exchange.setAttribute("ID_USUARIO", sessionUserId);
            request.setAttribute("ID_USUARIO", sessionUserId);
        }
        handler.handle(exchange);
        exchange.close();
    }

    @RequestMapping("/api/registro")
    public void registro(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(registroHandler, req, res);
    }

    @RequestMapping({"/api/guardar_reserva", "/api/turista/reservas", "/api/reservas"})
    public void guardarReserva(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if ("GET".equalsIgnoreCase(req.getMethod())) {
            delegate(obtenerReservasHandler, req, res);
        } else {
            delegate(guardarReservaHandler, req, res);
        }
    }

    @RequestMapping("/api/obtener_reservas")
    public void obtenerReservas(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(obtenerReservasHandler, req, res);
    }

    @RequestMapping("/api/cancelar_reserva")
    public void cancelarReserva(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(cancelarReservaHandler, req, res);
    }

    @RequestMapping("/api/calificar")
    public void calificar(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(calificarHandler, req, res);
    }

    @RequestMapping("/api/destinos")
    public void destinos(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(destinosHandler, req, res);
    }

    @RequestMapping("/api/destinos/tours")
    public void destinosTours(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(destinosToursHandler, req, res);
    }

    private final HttpHandler agenciasHandler = new com.travelink.controlador.admin.AgenciasAdminHandler();

    @RequestMapping({"/api/tours", "/api/tours/**"})
    public void tours(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(toursHandler, req, res);
    }

    @RequestMapping("/api/agencias")
    public void agencias(HttpServletRequest req, HttpServletResponse res) throws IOException {
        delegate(agenciasHandler, req, res);
    }
}
