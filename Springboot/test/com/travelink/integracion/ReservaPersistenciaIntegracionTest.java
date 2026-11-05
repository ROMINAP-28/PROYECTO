package com.travelink.integracion;

import com.travelink.config.ConexionDB;
import com.travelink.persistencia.ReservaPersistencia;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para ReservaPersistencia contra contenedor MySQL real (Testcontainers).
 * NOTA: La estructura de la tabla 'Pago' se carga directamente desde sql/travelink_schema.sql.
 */
@Testcontainers(disabledWithoutDocker = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReservaPersistenciaIntegracionTest {

    @Container
    @SuppressWarnings("resource")
    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("DBTravelink")
            .withUsername("testuser")
            .withPassword("testpass");

    private static ReservaPersistencia reservaPersistencia;
    private static int idReservaCreada = 0;
    private static final int ID_USUARIO_TEST = 1;

    @BeforeAll
    static void setUpAll() throws Exception {
        if (!mysql.isRunning()) {
            return;
        }

        // Configurar ConexionDB para que apunte EXCLUSIVAMENTE al contenedor de Testcontainers
        String jdbcUrl = mysql.getJdbcUrl() + "?allowMultiQueries=true&useSSL=false&serverTimezone=UTC";
        System.setProperty("DB_URL", jdbcUrl);
        System.setProperty("DB_USERNAME", mysql.getUsername());
        System.setProperty("DB_PASSWORD", mysql.getPassword());
        ConexionDB.cargarVariablesSiEsNecesario();

        reservaPersistencia = new ReservaPersistencia();

        // Cargar esquema completo desde sql/travelink_schema.sql (que ya incluye tabla Pago)
        File schemaFile = new File("sql/travelink_schema.sql");
        if (!schemaFile.exists()) {
            schemaFile = new File("../sql/travelink_schema.sql");
        }

        String schemaSql;
        if (schemaFile.exists()) {
            schemaSql = Files.readString(schemaFile.toPath(), StandardCharsets.UTF_8);
        } else {
            try (InputStream is = ReservaPersistenciaIntegracionTest.class.getResourceAsStream("/sql/travelink_schema.sql")) {
                if (is != null) {
                    schemaSql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                } else {
                    throw new IllegalStateException("No se encontró sql/travelink_schema.sql");
                }
            }
        }

        try (Connection conn = DriverManager.getConnection(jdbcUrl, mysql.getUsername(), mysql.getPassword());
             Statement stmt = conn.createStatement()) {
            // Ejecutar el script consolidado
            stmt.execute(schemaSql);

            // Insertar datos mínimos requeridos para pruebas de reserva
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
            stmt.execute("INSERT IGNORE INTO Rol (idRol, nombreRol) VALUES (1, 'TURISTA'), (2, 'AGENCIA');");
            stmt.execute("INSERT IGNORE INTO Persona (idPersona, nombre, apellidoPaterno, apellidoMaterno, nroDocumento, email) " +
                    "VALUES (1, 'Juan', 'Pérez', 'Gómez', '44556677', 'juan.perez.test@travelink.pe');");
            stmt.execute("INSERT IGNORE INTO Usuario (idUsuario, idPersona, idRol, nombreUsuario, contraseña, estado) " +
                    "VALUES (1, 1, 1, 'juan.perez.test@travelink.pe', 'hash123', 'Activo');");
            stmt.execute("INSERT IGNORE INTO Destino (idDestino, nombreDestino, departamento, descripcion, estado) " +
                    "VALUES (1, 'Cusco Arqueológico', 'Cusco', 'Ciudad imperial', 'Activo');");
            stmt.execute("INSERT IGNORE INTO Tour (idTour, nombreTour, idDestino, estado, precioAdulto, precioNino, aceptaBebes) " +
                    "VALUES (1, 'City Tour Cusco', 1, 'Activo', 150.00, 100.00, 0);");
            stmt.execute("INSERT IGNORE INTO TourFecha (idTourFecha, idTour, fecha, cupoTotal, cupoDisponible, estado) " +
                    "VALUES (1, 1, DATE_ADD(CURDATE(), INTERVAL 10 DAY), 20, 20, 'DISPONIBLE');");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");
        }
    }

    @Test
    @Order(1)
    @DisplayName("CP002-08: Debe guardar reserva completa y registrar el pago en la tabla Pago")
    void testGuardarReservaCompletaConPago() throws Exception {
        Assumptions.assumeTrue(mysql.isRunning(), "Docker no está disponible; omitiendo prueba de contenedor");

        Map<String, Object> reqData = new HashMap<>();
        reqData.put("sessionUserId", ID_USUARIO_TEST);
        reqData.put("idTourFecha", 1);
        reqData.put("idTour", 1);
        reqData.put("idAgencia", 1);
        reqData.put("cantAdultos", 2);
        reqData.put("cantNinos", 1);
        reqData.put("cantBebes", 0);
        reqData.put("precioAdulto", 150.00);
        reqData.put("precioNino", 100.00);
        reqData.put("precioBebe", 0.00);
        reqData.put("total", 400.00);
        reqData.put("tipoPago", "Tarjeta");
        reqData.put("numOperacion", "OP-TEST-778899");
        reqData.put("numTarjeta", "************4321");
        reqData.put("marcaTarjeta", "VISA");

        // Pasajeros
        List<Map<String, Object>> pasajeros = new ArrayList<>();
        Map<String, Object> p1 = new HashMap<>();
        p1.put("nombres", "Juan");
        p1.put("apellidos", "Pérez");
        p1.put("tipoDocumento", "DNI");
        p1.put("numeroDocumento", "44556677");
        p1.put("tipo", "ADULTO");
        pasajeros.add(p1);
        reqData.put("pasajeros", pasajeros);

        Map<String, Object> resultado = reservaPersistencia.guardarReservaCompleta(reqData);

        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals("success", resultado.get("status"), "Debe guardar con éxito");
        assertTrue(resultado.containsKey("idReserva"), "Debe retornar idReserva generado");

        idReservaCreada = Integer.parseInt(String.valueOf(resultado.get("idReserva")));
        assertTrue(idReservaCreada > 0, "El ID de reserva generado debe ser mayor a 0");

        // Verificar persistencia directa en la tabla 'Pago' en MySQL
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT monto, metodoPago, estado, numeroOperacion FROM Pago WHERE idReserva = ?")) {
            ps.setInt(1, idReservaCreada);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Debe existir un registro en la tabla Pago para la reserva creada");
                assertEquals(new BigDecimal("400.00"), rs.getBigDecimal("monto"));
                assertEquals("Tarjeta", rs.getString("metodoPago"));
                assertEquals("COMPLETADO", rs.getString("estado"));
                assertEquals("OP-TEST-778899", rs.getString("numeroOperacion"));
            }
        }
    }

    @Test
    @Order(2)
    @DisplayName("Debe verificar pertenencia de reserva al usuario creador")
    void testPerteneceAUsuario() {
        Assumptions.assumeTrue(mysql.isRunning() && idReservaCreada > 0, "Requiere reserva creada en contenedor");

        boolean esDuenio = reservaPersistencia.perteneceAUsuario(idReservaCreada, ID_USUARIO_TEST);
        boolean noEsDuenio = reservaPersistencia.perteneceAUsuario(idReservaCreada, 9999);

        assertTrue(esDuenio, "Debe pertenecer al usuario creador");
        assertFalse(noEsDuenio, "No debe pertenecer a un usuario ajeno");
    }

    @Test
    @Order(3)
    @DisplayName("Debe cancelar reserva actualizando estado a 'Cancelada' y estado de Pago a 'RECHAZADO'")
    void testCancelarReservaActualizaPagoYEstado() throws Exception {
        Assumptions.assumeTrue(mysql.isRunning() && idReservaCreada > 0, "Requiere reserva creada en contenedor");

        ReservaPersistencia.CancelarResultado cancelRes = reservaPersistencia.cancelarReservaConDetalle(
                idReservaCreada,
                "Cancelación voluntaria en prueba de integración"
        );

        assertNotNull(cancelRes);
        assertTrue(cancelRes.success, "La cancelación debe ser exitosa");

        // Verificar estado de Reserva y Pago en base de datos
        try (Connection conn = ConexionDB.getConnection()) {
            try (PreparedStatement psR = conn.prepareStatement("SELECT estado, motivoCancelacion FROM Reserva WHERE idReserva = ?")) {
                psR.setInt(1, idReservaCreada);
                try (ResultSet rsR = psR.executeQuery()) {
                    assertTrue(rsR.next());
                    assertEquals("Cancelada", rsR.getString("estado"));
                    assertEquals("Cancelación voluntaria en prueba de integración", rsR.getString("motivoCancelacion"));
                }
            }

            try (PreparedStatement psP = conn.prepareStatement("SELECT estado FROM Pago WHERE idReserva = ?")) {
                psP.setInt(1, idReservaCreada);
                try (ResultSet rsP = psP.executeQuery()) {
                    assertTrue(rsP.next());
                    assertEquals("RECHAZADO", rsP.getString("estado"));
                }
            }
        }
    }
}

