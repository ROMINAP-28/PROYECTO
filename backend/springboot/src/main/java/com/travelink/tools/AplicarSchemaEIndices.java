package com.travelink.tools;

import com.travelink.config.ConexionDB;
import java.sql.*;

public class AplicarSchemaEIndices {

    public static void main(String[] args) {
        System.out.println("=== APLICANDO ALTER TABLE E ÍNDICES EN DBTravelink ===");

        try (Connection con = ConexionDB.getConnection()) {
            if (con == null) {
                System.err.println("No se pudo conectar a la base de datos.");
                return;
            }

            Statement st = con.createStatement();

            // 1. Agregar columnas a Calificacion para Flageado / Calidad
            agregarColumnaSiNoExiste(con, "Calificacion", "flageada", "TINYINT(1) DEFAULT 0");
            agregarColumnaSiNoExiste(con, "Calificacion", "motivoFlag", "VARCHAR(255) NULL");
            agregarColumnaSiNoExiste(con, "Calificacion", "fechaFlag", "DATETIME NULL");
            agregarColumnaSiNoExiste(con, "Calificacion", "estadoRevision", "VARCHAR(50) DEFAULT 'mantenida'");

            // Auto-flagear calificaciones existentes de 1 y 2 estrellas
            st.executeUpdate(
                "UPDATE Calificacion SET flageada = 1, motivoFlag = 'Calificación baja (1-2 estrellas)', estadoRevision = 'pendiente' " +
                "WHERE estrellas <= 2 AND (flageada IS NULL OR flageada = 0)"
            );

            // 2. Agregar columna periodo a Comision
            agregarColumnaSiNoExiste(con, "Comision", "periodo", "VARCHAR(20) DEFAULT '2026-10'");
            agregarColumnaSiNoExiste(con, "Comision", "ventasTotales", "DECIMAL(10,2) DEFAULT 0.00");
            agregarColumnaSiNoExiste(con, "Comision", "montoTravelink", "DECIMAL(10,2) DEFAULT 0.00");

            // 3. Crear Índices de Optimización (Ignorando duplicados si ya existen)
            crearIndiceSiNoExiste(st, "idx_reserva_idagencia", "Reserva", "idAgencia");
            crearIndiceSiNoExiste(st, "idx_reserva_idusuario", "Reserva", "idUsuario");
            crearIndiceSiNoExiste(st, "idx_reserva_estado", "Reserva", "estado");
            crearIndiceSiNoExiste(st, "idx_calificacion_idagencia", "Calificacion", "idAgencia");
            crearIndiceSiNoExiste(st, "idx_calificacion_idreserva", "Calificacion", "idReserva");
            crearIndiceSiNoExiste(st, "idx_comision_idagencia", "Comision", "idAgencia");
            crearIndiceSiNoExiste(st, "idx_persona_email", "Persona", "email");
            crearIndiceSiNoExiste(st, "idx_persona_nrodoc", "Persona", "nroDocumento");

            System.out.println("=== ¡SCHEMA E ÍNDICES APLICADOS CORRECTAMENTE! ===");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void agregarColumnaSiNoExiste(Connection con, String tabla, String columna, String definicion) {
        try {
            DatabaseMetaData meta = con.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, tabla, columna)) {
                if (!rs.next()) {
                    String sql = "ALTER TABLE `" + tabla + "` ADD COLUMN `" + columna + "` " + definicion;
                    con.createStatement().executeUpdate(sql);
                    System.out.println("  + Agregada columna " + columna + " a " + tabla);
                } else {
                    System.out.println("  . Columna " + columna + " ya existe en " + tabla);
                }
            }
        } catch (SQLException e) {
            System.err.println("  ! Error al agregar columna " + columna + ": " + e.getMessage());
        }
    }

    private static void crearIndiceSiNoExiste(Statement st, String nombreIndice, String tabla, String columna) {
        try {
            st.executeUpdate("CREATE INDEX `" + nombreIndice + "` ON `" + tabla + "` (`" + columna + "`)");
            System.out.println("  + Creado índice " + nombreIndice + " en " + tabla);
        } catch (SQLException e) {
            System.out.println("  . Índice " + nombreIndice + " ya existía o ignorado.");
        }
    }
}
