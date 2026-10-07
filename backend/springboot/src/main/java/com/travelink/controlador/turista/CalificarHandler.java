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

    public class CalificarHandler implements HttpHandler {
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

                com.travelink.entidades.Calificacion c = new com.travelink.entidades.Calificacion();
                try { c.setIdUsuario(Integer.parseInt(String.valueOf(params.getOrDefault("idUsuario", "1")))); } catch (Exception ignored) {}
                try { c.setIdAgencia(Integer.parseInt(String.valueOf(params.getOrDefault("idAgencia", "1")))); } catch (Exception ignored) {}
                try { c.setIdReserva(Integer.parseInt(String.valueOf(params.getOrDefault("idReserva", "1")))); } catch (Exception ignored) {}
                try { c.setEstrellas(Integer.parseInt(String.valueOf(params.getOrDefault("estrellas", "5")))); } catch (Exception ignored) {}
                c.setComentario(String.valueOf(params.getOrDefault("comentario", "")));

                Map<String, Object> res = JavaApiServer.calificacionControlador.guardarCalificacion(c);
                if ("success".equals(res.get("status"))) {
                    JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Calificación registrada exitosamente\"}");
                } else {
                    JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"error\",\"message\":\"" + res.get("message") + "\"}");
                }
            } else {
                JavaApiServer.sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
            }
        }
    }

