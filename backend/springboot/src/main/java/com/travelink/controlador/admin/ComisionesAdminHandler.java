package com.travelink.controlador.admin;
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

    public class ComisionesAdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JavaApiServer.enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            StringBuilder json = new StringBuilder("{\"status\":\"success\",\"comisiones\":[");
            boolean primero = true;
            String sql = """
                SELECT 
                    a.idAgencia AS id, 
                    COALESCE(NULLIF(a.razonSocial, ''), a.nombreComercial, 'Agencia Travelink') AS agencia, 
                    COALESCE(a.ruc, '20556677889') AS ruc,
                    'Octubre 2026' AS periodo,
                    COALESCE(SUM(r.total), 0) AS ventasTotales,
                    15.00 AS comisionPct,
                    COALESCE(SUM(r.total * 0.15), 0) AS montoComision,
                    'Pendiente' AS estado
                FROM Agencia a 
                LEFT JOIN Reserva r ON a.idAgencia = r.idAgencia AND UPPER(r.estado) IN ('CONFIRMADA', 'ACTIVA', 'PENDIENTE', 'FINALIZADA') 
                WHERE a.estado IN ('ACTIVO', 'Aceptado') 
                GROUP BY a.idAgencia, a.razonSocial, a.nombreComercial, a.ruc 
                ORDER BY a.idAgencia ASC
            """;
            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!primero) json.append(",");
                    primero = false;
                    json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"agencia\":\"").append(JavaApiServer.jsonEscape(rs.getString("agencia"))).append("\",")
                        .append("\"ruc\":\"").append(JavaApiServer.jsonEscape(rs.getString("ruc"))).append("\",")
                        .append("\"periodo\":\"").append(JavaApiServer.jsonEscape(rs.getString("periodo"))).append("\",")
                        .append("\"ventasTotales\":").append(rs.getBigDecimal("ventasTotales")).append(",")
                        .append("\"comisionPct\":15.00,")
                        .append("\"montoComision\":").append(rs.getBigDecimal("montoComision")).append(",")
                        .append("\"estado\":\"").append(JavaApiServer.jsonEscape(rs.getString("estado"))).append("\"")
                        .append("}");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            json.append("]}");
            JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
        }
    }

