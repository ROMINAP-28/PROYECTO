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

        String cleanUrl = ConexionDB.getUrl();
        String cleanUser = ConexionDB.getUser();
        String cleanPass = ConexionDB.getPassword();

        System.setProperty("CLEAN_DB_URL", cleanUrl);
        System.setProperty("spring.datasource.url", cleanUrl);
        System.setProperty("spring.datasource.username", cleanUser);
        System.setProperty("spring.datasource.password", cleanPass);

        log.info("Iniciando TRAVELINK con DB_URL sanitizada: {}", cleanUrl.split("\\?")[0]);

        SpringApplication app = new SpringApplication(TravelinkApplication.class);
        java.util.Map<String, Object> props = new java.util.HashMap<>();
        props.put("CLEAN_DB_URL", cleanUrl);
        props.put("spring.datasource.url", cleanUrl);
        props.put("spring.datasource.username", cleanUser);
        props.put("spring.datasource.password", cleanPass);
        app.setDefaultProperties(props);
        app.run(args);
    }

    @org.springframework.context.annotation.Bean
    @org.springframework.context.annotation.Primary
    public javax.sql.DataSource dataSource() {
        String jdbcUrl = ConexionDB.getUrl();
        String user = ConexionDB.getUser();
        String pass = ConexionDB.getPassword();
        System.out.println("=================================================");
        System.out.println("CONFIGURING HIKARI DATA SOURCE:");
        System.out.println("  JDBC URL: " + jdbcUrl);
        System.out.println("  USERNAME: " + user);
        System.out.println("=================================================");

        com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(pass);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setPoolName("TravelinkHikariPool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setMaxLifetime(180000);
        config.setConnectionTimeout(30000);
        config.setInitializationFailTimeout(10000);
        com.zaxxer.hikari.HikariDataSource ds = new com.zaxxer.hikari.HikariDataSource(config);
        ConexionDB.setDataSource(ds);
        return ds;
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner initConexionDbDataSource(javax.sql.DataSource ds) {
        return args -> {
            ConexionDB.setDataSource(ds);
            log.info("ConexionDB verificado exitosamente con el pool HikariCP de Spring Boot.");
            crearIndicesSiNoExisten(ds);
        };
    }

    private void crearIndicesSiNoExisten(javax.sql.DataSource ds) {
        String[] indices = {
            "CREATE INDEX idx_reserva_usuario ON Reserva(idUsuario)",
            "CREATE INDEX idx_reserva_agencia ON Reserva(idAgencia)",
            "CREATE INDEX idx_reserva_tour ON Reserva(idTour)",
            "CREATE INDEX idx_reserva_tourfecha ON Reserva(idTourFecha)",
            "CREATE INDEX idx_reserva_estado ON Reserva(estado)",
            "CREATE INDEX idx_tour_agencia ON Tour(idAgencia)",
            "CREATE INDEX idx_tourfecha_tour ON TourFecha(idTour)",
            "CREATE INDEX idx_calificacion_agencia ON Calificacion(idAgencia)",
            "CREATE INDEX idx_calificacion_usuario ON Calificacion(idUsuario)",
            "CREATE INDEX idx_calificacion_reserva ON Calificacion(idReserva)"
        };
        try (java.sql.Connection con = ds.getConnection()) {
            for (String sql : indices) {
                try (java.sql.Statement st = con.createStatement()) {
                    st.execute(sql);
                } catch (java.sql.SQLException ignored) {
                    // Ya existe el índice en MySQL
                }
            }
            log.info("Verificación y optimización de índices de BD completada exitosamente.");
        } catch (Exception e) {
            log.warn("Verificación de índices completada con aviso: {}", e.getMessage());
        }
    }
}
