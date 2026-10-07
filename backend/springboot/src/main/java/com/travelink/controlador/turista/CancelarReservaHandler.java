package com.travelink.controlador.turista;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import com.travelink.entidades.Usuario;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import com.travelink.config.ConexionDB;
import com.travelink.server.JavaApiServer;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import com.travelink.controlador.UsuarioControlador;
import com.travelink.controlador.CalificacionControlador;
import com.travelink.repositorio.ReservaRepositorio;

    public class CancelarReservaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                JavaApiServer.enableCORS(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = JavaApiServer.readRequestBody(exchange);
                Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);

                int idReserva = 0;
                try { idReserva = Integer.parseInt(String.valueOf(params.get("idReserva"))); } catch (Exception ignored) {}
                String motivo = String.valueOf(params.getOrDefault("motivo", "Cancelación solicitada por el usuario"));

                boolean ok = JavaApiServer.reservaRepositorio.cancelarReserva(idReserva, motivo);
                if (ok) {
                    JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Reserva cancelada correctamente\"}");
                } else {
                    JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"error\",\"message\":\"No se pudo cancelar la reserva\"}");
                }
            } else {
                JavaApiServer.sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
            }
        }
    }

