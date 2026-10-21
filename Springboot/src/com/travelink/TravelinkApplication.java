package com.travelink;

import com.travelink.config.ConexionDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal de la aplicación TRAVELINK.
 * Configura la carga de variables de entorno seguras, valida la conectividad
 * inicial con la base de datos MySQL e inicializa el contenedor Spring Boot.
 */
@SpringBootApplication
public class TravelinkApplication {

    private static final Logger log = LoggerFactory.getLogger(TravelinkApplication.class);

    public static void main(String[] args) {
        // Cargar variables de entorno desde .env si no existen en el sistema
        ConexionDB.cargarVariablesSiEsNecesario();

        // Validar variables requeridas de base de datos
        String dbUrl = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : System.getProperty("DB_URL");
        String dbUser = System.getenv("DB_USERNAME") != null ? System.getenv("DB_USERNAME")
                : (System.getenv("DB_USER") != null ? System.getenv("DB_USER") : System.getProperty("DB_USERNAME"));
        String dbPass = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : System.getProperty("DB_PASSWORD");

        if (dbUrl == null || dbUrl.isBlank() || dbUser == null || dbUser.isBlank() || dbPass == null || dbPass.isBlank()) {
            log.error("================================================================================");
            log.error("[ERROR FATAL DE INICIO] Faltan variables de entorno obligatorias para la base de datos:");
            if (dbUrl == null || dbUrl.isBlank()) log.error("  -> Falta DB_URL");
            if (dbUser == null || dbUser.isBlank()) log.error("  -> Falta DB_USERNAME");
            if (dbPass == null || dbPass.isBlank()) log.error("  -> Falta DB_PASSWORD");
            log.error("Configure estas variables en el sistema o en el archivo .env en la raíz del proyecto.");
            log.error("Puede consultar el archivo .env.example como plantilla de referencia.");
            log.error("================================================================================");
            throw new IllegalStateException("Faltan variables de entorno obligatorias para la base de datos (DB_URL, DB_USERNAME, DB_PASSWORD).");
        }

        SpringApplication.run(TravelinkApplication.class, args);
    }
}
