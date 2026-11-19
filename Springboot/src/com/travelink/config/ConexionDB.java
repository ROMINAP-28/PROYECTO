package com.travelink.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestor de conexiones JDBC para consultas especializadas.
 * Las credenciales se leen EXCLUSIVAMENTE de las variables de entorno
 * DB_URL, DB_USERNAME y DB_PASSWORD (o desde el archivo .env local).
 * No contiene valores por defecto ni imprime credenciales en logs.
 */
public class ConexionDB {

    private static final Logger log = LoggerFactory.getLogger(ConexionDB.class);
    private static volatile javax.sql.DataSource pooledDataSource;

    public static void setDataSource(javax.sql.DataSource ds) {
        pooledDataSource = ds;
        log.info("Pool de conexiones DataSource asignado exitosamente a ConexionDB.");
    }

    static {
        cargarVariablesSiEsNecesario();
    }

    public static synchronized void cargarVariablesSiEsNecesario() {
        if (obtenerValor("DB_URL") == null || obtenerValor("DB_USERNAME") == null || obtenerValor("DB_PASSWORD") == null) {
            // Intentar cargar desde archivo .env local si existe
            File envFile = new File(".env");
            if (!envFile.exists()) {
                envFile = new File("../.env");
            }
            if (envFile.exists() && envFile.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(envFile, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        int eqIdx = line.indexOf('=');
                        if (eqIdx > 0) {
                            String key = line.substring(0, eqIdx).trim();
                            String value = line.substring(eqIdx + 1).trim();
                            if ((key.equals("DB_URL") || key.equals("DB_USERNAME") || key.equals("DB_PASSWORD") || key.equals("DB_USER"))
                                    && System.getProperty(key) == null) {
                                System.setProperty(key, value);
                            }
                        }
                    }
                    log.info("Variables de entorno para base de datos cargadas exitosamente desde archivo .env");
                } catch (Exception e) {
                    log.warn("No se pudo leer el archivo .env: {}", e.getMessage());
                }
            }
        }
    }

    private static String obtenerValor(String key) {
        String val = System.getenv(key);
        if (val != null && !val.isBlank()) {
            return val;
        }
        val = System.getProperty(key);
        if (val != null && !val.isBlank()) {
            return val;
        }
        return null;
    }

    public static String getUrl() {
        cargarVariablesSiEsNecesario();
        String val = System.getProperty("spring.datasource.url");
        if (val == null || val.isBlank()) {
            val = obtenerValor("DB_URL");
        }
        if (val == null || val.isBlank()) {
            val = "jdbc:mysql://mysql-traveling-traveling.k.aivencloud.com:19936/DBTravelink?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=America/Lima";
        }
        val = val.trim();
        if (val.endsWith("?")) {
            val = val.substring(0, val.length() - 1);
        }
        if (val.contains("traveling.k.aivencloud.com") && !val.contains("mysql-traveling-traveling.k.aivencloud.com")) {
            val = val.replace("traveling.k.aivencloud.com", "mysql-traveling-traveling.k.aivencloud.com");
        }
        if (!val.startsWith("jdbc:")) {
            val = "jdbc:mysql://" + val;
        }
        if (!val.contains("?") && val.contains("aivencloud.com")) {
            val = val + "?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=America/Lima";
        }
        System.setProperty("DB_URL", val);
        System.setProperty("spring.datasource.url", val);
        return val;
    }

    public static String getUser() {
        cargarVariablesSiEsNecesario();
        String val = obtenerValor("DB_USERNAME");
        if (val == null || val.isBlank()) {
            val = obtenerValor("DB_USER");
        }
        if (val == null || val.isBlank()) {
            val = "avnadmin";
        }
        return val;
    }

    public static String getPassword() {
        cargarVariablesSiEsNecesario();
        String val = obtenerValor("DB_PASSWORD");
        if (val == null || val.isBlank()) {
            log.error("ERROR FATAL: La variable de entorno obligatoria 'DB_PASSWORD' no está definida. La aplicación requiere esta variable para conectarse a la base de datos.");
            throw new IllegalStateException("Falta la variable de entorno obligatoria: DB_PASSWORD");
        }
        return val;
    }

    public static Connection getConnection() {
        if (pooledDataSource != null) {
            try {
                return pooledDataSource.getConnection();
            } catch (SQLException e) {
                log.warn("Fallo al obtener conexion desde HikariDataSource pool ({}), intentando DriverManager directo...", e.getMessage());
            }
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(getUrl(), getUser(), getPassword());
        } catch (ClassNotFoundException e) {
            log.error("No se encontró el driver JDBC de MySQL (com.mysql.cj.jdbc.Driver).");
            return null;
        } catch (SQLException e) {
            log.error("Error al establecer conexión JDBC con la base de datos ({}): código estado SQL={}, código error={}",
                    e.getMessage(), e.getSQLState(), e.getErrorCode());
            return null;
        }
    }
}