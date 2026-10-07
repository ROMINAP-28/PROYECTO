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

    public class CalidadAdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JavaApiServer.enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            StringBuilder json = new StringBuilder("{\"status\":\"success\",\"data\":{");
            
            // 1. REVIEWS
            json.append("\"reviews\":[");
            boolean primeroRev = true;
            String sqlRev = """
                SELECT 
                    c.idCalificacion AS id,
                    CONCAT(p.nombre, ' ', p.apellidoPaterno) AS usuario,
                    p.email,
                    COALESCE(NULLIF(a.nombreComercial, ''), a.razonSocial, 'Agencia Travelink') AS agencia,
                    COALESCE(t.nombre, r.nombreTour, 'Tour Turístico') AS tour,
                    c.estrellas,
                    COALESCE(c.comentario, 'Sin comentario') AS comentario,
                    DATE_FORMAT(c.fechaCalificacion, '%d %b. %Y - %H:%i') AS fecha,
                    IF(c.eliminadoPorAdmin = 1, 'Eliminado', 'Pendiente') AS estado
                FROM Calificacion c
                JOIN Usuario u ON c.idUsuario = u.idUsuario
                JOIN Persona p ON u.idPersona = p.idPersona
                JOIN Agencia a ON c.idAgencia = a.idAgencia
                LEFT JOIN Reserva r ON c.idReserva = r.idReserva
                LEFT JOIN Tour t ON r.idTour = t.idTour
                ORDER BY c.idCalificacion DESC
            """;

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sqlRev);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!primeroRev) json.append(",");
                    primeroRev = false;
                    json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"usuario\":\"").append(JavaApiServer.jsonEscape(rs.getString("usuario"))).append("\",")
                        .append("\"email\":\"").append(JavaApiServer.jsonEscape(rs.getString("email"))).append("\",")
                        .append("\"agencia\":\"").append(JavaApiServer.jsonEscape(rs.getString("agencia"))).append("\",")
                        .append("\"tour\":\"").append(JavaApiServer.jsonEscape(rs.getString("tour"))).append("\",")
                        .append("\"estrellas\":").append(rs.getDouble("estrellas")).append(",")
                        .append("\"comentario\":\"").append(JavaApiServer.jsonEscape(rs.getString("comentario"))).append("\",")
                        .append("\"fecha\":\"").append(JavaApiServer.jsonEscape(rs.getString("fecha"))).append("\",")
                        .append("\"estado\":\"").append(JavaApiServer.jsonEscape(rs.getString("estado"))).append("\"")
                        .append("}");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            json.append("],");

            // 2. TOURS EN REVISION
            json.append("\"toursEnRevision\":[");
            boolean primeroTour = true;
            String sqlTours = """
                SELECT 
                    t.idTour AS id,
                    t.nombre AS tour,
                    t.duracion,
                    COALESCE(NULLIF(a.nombreComercial, ''), a.razonSocial) AS agencia,
                    a.ruc,
                    d.nombre AS destino,
                    COALESCE(t.calificacionPromedio, 4.5) AS califTour,
                    COALESCE(a.promedioCalificacion, 4.8) AS califAgencia,
                    t.precioAdulto AS precio,
                    t.estado,
                    'Supervisión rutinaria de calidad e itinerario' AS motivo,
                    COALESCE(ti.url, '../../img/lima.jpg') AS imagen
                FROM Tour t
                JOIN Agencia a ON t.idAgencia = a.idAgencia
                JOIN Destino d ON t.idDestino = d.idDestino
                LEFT JOIN TourImagen ti ON t.idTour = ti.idTour AND ti.esPrincipal = 1
                ORDER BY t.idTour DESC
            """;

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sqlTours);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!primeroTour) json.append(",");
                    primeroTour = false;
                    json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"tour\":\"").append(JavaApiServer.jsonEscape(rs.getString("tour"))).append("\",")
                        .append("\"duracion\":\"").append(JavaApiServer.jsonEscape(rs.getString("duracion"))).append("\",")
                        .append("\"agencia\":\"").append(JavaApiServer.jsonEscape(rs.getString("agencia"))).append("\",")
                        .append("\"ruc\":\"").append(JavaApiServer.jsonEscape(rs.getString("ruc"))).append("\",")
                        .append("\"destino\":\"").append(JavaApiServer.jsonEscape(rs.getString("destino"))).append("\",")
                        .append("\"califTour\":").append(rs.getDouble("califTour")).append(",")
                        .append("\"califAgencia\":").append(rs.getDouble("califAgencia")).append(",")
                        .append("\"precio\":").append(rs.getBigDecimal("precio")).append(",")
                        .append("\"estado\":\"").append(JavaApiServer.jsonEscape(rs.getString("estado"))).append("\",")
                        .append("\"motivo\":\"").append(JavaApiServer.jsonEscape(rs.getString("motivo"))).append("\",")
                        .append("\"imagen\":\"").append(JavaApiServer.jsonEscape(rs.getString("imagen"))).append("\"")
                        .append("}");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            json.append("]}}");

            JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
        }
    }

