package com.travelink.controlador.agencia;
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

    public class DestinosAgenciaHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                JavaApiServer.enableCORS(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                JavaApiServer.sendJsonResponse(exchange, 405,
                        "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
                return;
            }

            StringBuilder json = new StringBuilder();
            json.append("{\"status\":\"success\",\"destinos\":[");

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT idDestino, nombre FROM Destino WHERE estado = 'ACTIVO' AND nombre IN ('Lima', 'Pasco', 'Tacna', 'Moquegua', 'Ilo', 'Madre de Dios') ORDER BY idDestino");
                 ResultSet rs = ps.executeQuery()) {

                boolean primero = true;
                while (rs.next()) {
                    if (!primero) json.append(",");
                    json.append("{")
                            .append("\"idDestino\":").append(rs.getInt("idDestino")).append(",")
                            .append("\"nombre\":\"").append(JavaApiServer.jsonEscape(rs.getString("nombre"))).append("\"")
                            .append("}");
                    primero = false;
                }

                json.append("]}");
                JavaApiServer.sendJsonResponse(exchange, 200, json.toString());

            } catch (Exception e) {
                e.printStackTrace();
                JavaApiServer.sendJsonResponse(exchange, 500,
                        "{\"status\":\"error\",\"message\":\"Error al obtener los destinos\"}");
            }
        }
    }

