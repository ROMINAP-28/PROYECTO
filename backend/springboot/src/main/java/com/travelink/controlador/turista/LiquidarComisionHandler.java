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

    public class LiquidarComisionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JavaApiServer.enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            Map<String, String> params = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());
            int id = 0;
            try { id = Integer.parseInt(params.getOrDefault("id", "0")); } catch (Exception ignored) {}

            if (id > 0) {
                try (Connection con = ConexionDB.getConnection();
                     PreparedStatement ps = con.prepareStatement("UPDATE Comision SET estado = 'Liquidado' WHERE idAgencia = ? OR idComision = ?")) {
                    ps.setInt(1, id);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Comisión liquidada exitosamente.\"}");
        }
    }

