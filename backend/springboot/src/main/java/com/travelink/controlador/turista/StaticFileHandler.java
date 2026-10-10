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

    public class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.equals("/index.html")) {
                path = "/index.html";
            }

            String relativePath = path.startsWith("/") ? path.substring(1) : path;
            
            // Buscar la carpeta static de forma robusta subiendo en el árbol si es necesario
            Path currentPath = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
            Path staticDir = null;
            
            for (int i = 0; i < 4; i++) {
                Path checkDir = currentPath.resolve("backend/springboot/src/main/resources/static");
                if (Files.exists(checkDir)) {
                    staticDir = checkDir;
                    break;
                }
                checkDir = currentPath.resolve("src/main/resources/static");
                if (Files.exists(checkDir)) {
                    staticDir = checkDir;
                    break;
                }
                checkDir = currentPath.resolve("springboot/src/main/resources/static");
                if (Files.exists(checkDir)) {
                    staticDir = checkDir;
                    break;
                }
                currentPath = currentPath.getParent();
                if (currentPath == null) break;
            }
            
            if (staticDir == null) {
                String notFound = "<h1>Error 500</h1><p>No se pudo encontrar el directorio estático del frontend en el servidor.</p>";
                exchange.sendResponseHeaders(500, notFound.length());
                OutputStream os = exchange.getResponseBody();
                os.write(notFound.getBytes(StandardCharsets.UTF_8));
                os.close();
                return;
            }

            Path filePath = staticDir.resolve(relativePath).normalize();
            File file = filePath.toFile();

            if (!file.exists() || file.isDirectory()) {
                String notFound = "<h1>404 File Not Found</h1><p>Requested: " + path + "</p><p>Tried absolute path: " + file.getAbsolutePath() + "</p>";
                exchange.sendResponseHeaders(404, notFound.length());
                OutputStream os = exchange.getResponseBody();
                os.write(notFound.getBytes(StandardCharsets.UTF_8));
                os.close();
                return;
            }

            String contentType = getContentType(file.getName());
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, file.length());

            OutputStream os = exchange.getResponseBody();
            Files.copy(file.toPath(), os);
            os.close();
        }

        private String getContentType(String filename) {
            if (filename.endsWith(".html")) return "text/html; charset=UTF-8";
            if (filename.endsWith(".css")) return "text/css; charset=UTF-8";
            if (filename.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
            if (filename.endsWith(".png")) return "image/png";
            if (filename.endsWith(".svg")) return "image/svg+xml";
            if (filename.endsWith(".ico")) return "image/x-icon";
            return "application/octet-stream";
        }
    }

