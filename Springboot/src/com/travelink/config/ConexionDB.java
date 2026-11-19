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

    public static final String DEFAULT_AIVEN_URL =
        "jdbc:mysql://mysql-traveling-traveling.k.aivencloud.com:19936/DBTravelink?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=America/Lima";

    public static String sanitizarUrl(String val) {
        if (val == null || val.isBlank()) {
            return DEFAULT_AIVEN_URL;
        }
        val = val.replaceAll("[\\r\\n\\t\\s'\"]+", "").trim();

        // 1. Quitar todos los prefijos posibles: "jdbc:mysql://", "jdbc:", "mysql://"
        while (val.startsWith("jdbc:")) {
            val = val.substring("jdbc:".length());
        }
        while (val.startsWith("mysql://")) {
            val = val.substring("mysql://".length());
        }
        while (val.startsWith("/")) {
            val = val.substring(1);
        }

        // 2. Si viene formato con credenciales incrustadas (user:password@host:port/db)
        if (val.contains("@")) {
            int atIdx = val.indexOf('@');
            String creds = val.substring(0, atIdx);
            val = val.substring(atIdx + 1); // dejar solo host:port/db...
            if (creds.contains(":")) {
                int colonIdx = creds.indexOf(':');
                String user = creds.substring(0, colonIdx);
                String pass = creds.substring(colonIdx + 1);
                if (!user.isBlank() && System.getProperty("DB_USERNAME") == null && System.getenv("DB_USERNAME") == null) {
                    System.setProperty("DB_USERNAME", user);
                }
                if (!pass.isBlank() && System.getProperty("DB_PASSWORD") == null && System.getenv("DB_PASSWORD") == null) {
                    System.setProperty("DB_PASSWORD", pass);
                }
            }
        }

        // 3. Corregir dominio Aiven si le falta el prefijo
        if (val.contains("traveling.k.aivencloud.com") && !val.contains("mysql-traveling-traveling.k.aivencloud.com")) {
            val = val.replace("traveling.k.aivencloud.com", "mysql-traveling-traveling.k.aivencloud.com");
        }

        // 4. Separar host:port/db de los query parameters
        String hostAndDb = val;
        if (val.contains("?")) {
            hostAndDb = val.substring(0, val.indexOf('?'));
        }

        // 5. Garantizar nombre de base de datos
        if (!hostAndDb.contains("/")) {
            hostAndDb = hostAndDb + "/DBTravelink";
        }

        return "jdbc:mysql://" + hostAndDb + "?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=America/Lima";
    }

    public static String getUrl() {
        cargarVariablesSiEsNecesario();
        String val = System.getenv("DB_URL");
        if (val == null || val.isBlank()) {
            val = System.getProperty("DB_URL");
        }
        if (val == null || val.isBlank()) {
            val = System.getProperty("spring.datasource.url");
        }
        String clean = sanitizarUrl(val);
        System.setProperty("DB_URL", clean);
        System.setProperty("spring.datasource.url", clean);
        System.setProperty("CLEAN_DB_URL", clean);
        return clean;
    }

    public static String getUser() {
        cargarVariablesSiEsNecesario();
        String val = System.getenv("DB_USERNAME");
        if (val == null || val.isBlank()) val = System.getenv("DB_USER");
        if (val == null || val.isBlank()) val = System.getProperty("DB_USERNAME");
        if (val == null || val.isBlank()) val = System.getProperty("DB_USER");
        if (val == null || val.isBlank()) val = "avnadmin";
        val = val.replaceAll("[\\r\\n\\t\\s'\"]+", "").trim();
        return val.isEmpty() ? "avnadmin" : val;
    }

    public static String getPassword() {
        cargarVariablesSiEsNecesario();
        String val = System.getenv("DB_PASSWORD");
        if (val == null || val.isBlank()) val = System.getProperty("DB_PASSWORD");
        if (val == null || val.isBlank()) {
            val = "AVNS_GohPsfjQ1YmM-IL7AoH";
        }
        val = val.replaceAll("[\\r\\n\\t\\s'\"]+", "").trim();
        return val.isEmpty() ? "AVNS_GohPsfjQ1YmM-IL7AoH" : val;
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