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

    public class GuardarReservaHandler implements HttpHandler {
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
                Map<String, Object> res = JavaApiServer.reservaRepositorio.guardarReservaCompleta(params);

                StringBuilder json = new StringBuilder();
                if ("success".equals(res.get("status"))) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> d = (Map<String, Object>) res.get("data");
                    json.append("{\"status\":\"success\",\"message\":\"Reserva guardada exitosamente\",\"data\":{")

                            .append("\"idReserva\":").append(d.get("idReserva")).append(",")
                            .append("\"idPago\":").append(d.get("idPago")).append(",")
                            .append("\"codigo\":\"").append(d.get("codigo")).append("\",")
                            .append("\"estado\":\"").append(d.get("estado")).append("\",")
                            .append("\"precioTotal\":").append(d.get("total")).append(",")
                            .append("\"metodoPago\":\"").append(d.get("metodoPago")).append("\"")
                            .append("}}");

                } else {
                    json.append("{\"status\":\"error\",\"message\":\"").append(res.get("message")).append("\"}");
                }
                JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
            } else {
                JavaApiServer.sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
            }
        }
    }

