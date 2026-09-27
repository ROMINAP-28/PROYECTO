package com.travelink.tools;

import com.travelink.config.ConexionDB;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class MigrarNotificacionesDB {
    public static void main(String[] args) {
        System.out.println("=== CONFIGURANDO TABLA NOTIFICACION EN BASE DE DATOS ===");
        try (Connection con = ConexionDB.getConnection();
             Statement stmt = con.createStatement()) {

            DatabaseMetaData meta = con.getMetaData();
            Set<String> cols = new HashSet<>();
            try (ResultSet rs = meta.getColumns(null, null, "Notificacion", null)) {
                while (rs.next()) {
                    cols.add(rs.getString("COLUMN_NAME").toLowerCase());
                }
            }

            if (!cols.contains("idusuario")) {
                stmt.execute("ALTER TABLE Notificacion ADD COLUMN idUsuario INT NULL;");
            }
            if (!cols.contains("titulo")) {
                stmt.execute("ALTER TABLE Notificacion ADD COLUMN titulo VARCHAR(150) NOT NULL;");
            }
            if (!cols.contains("mensaje")) {
                stmt.execute("ALTER TABLE Notificacion ADD COLUMN mensaje TEXT NULL;");
            }
            if (!cols.contains("tipo")) {
                stmt.execute("ALTER TABLE Notificacion ADD COLUMN tipo VARCHAR(50) NOT NULL DEFAULT 'INFO';");
            }
            if (!cols.contains("icono")) {
                stmt.execute("ALTER TABLE Notificacion ADD COLUMN icono VARCHAR(50) DEFAULT 'ti ti-bell';");
            }
            if (!cols.contains("color")) {
                stmt.execute("ALTER TABLE Notificacion ADD COLUMN color VARCHAR(30) DEFAULT 'blue';");
            }
            if (!cols.contains("enlace")) {
                stmt.execute("ALTER TABLE Notificacion ADD COLUMN enlace VARCHAR(255) NULL;");
            }
            if (!cols.contains("leida")) {
                stmt.execute("ALTER TABLE Notificacion ADD COLUMN leida BOOLEAN NOT NULL DEFAULT FALSE;");
            }
            if (!cols.contains("fechaenvio")) {
                stmt.execute("ALTER TABLE Notificacion ADD COLUMN fechaEnvio DATETIME DEFAULT CURRENT_TIMESTAMP;");
            }

            // Verificar si hay registros
            ResultSet rsCount = stmt.executeQuery("SELECT COUNT(*) FROM Notificacion;");
            int count = 0;
            if (rsCount.next()) count = rsCount.getInt(1);

            if (count == 0) {
                stmt.execute("INSERT INTO Notificacion (idUsuario, titulo, mensaje, tipo, icono, color, enlace, leida, fechaEnvio) VALUES " +
                        "(4, 'Nueva Agencia', 'Andes Tours ha completado la actualización de sus datos comerciales.', 'AGENCIA', 'ti ti-building-store', 'blue', 'agencias.html', FALSE, NOW() - INTERVAL 10 MINUTE)," +
                        "(4, 'Nueva Reserva Confirmada', 'Reserva TRK-001 confirmada para el tour Machu Picchu Clásico por S/ 1,050.00.', 'RESERVA', 'ti ti-calendar-event', 'green', 'reservas.html', FALSE, NOW() - INTERVAL 25 MINUTE)," +
                        "(4, 'Nueva Calificación Recibida', 'Inka Travel recibió una calificación de 5 estrellas con comentario positivo.', 'CALIDAD', 'ti ti-star', 'orange', 'calidad.html', FALSE, NOW() - INTERVAL 1 HOUR)," +
                        "(4, 'Liquidación de Comisiones', 'El cálculo mensual de comisiones automáticas está disponible para revisión.', 'SISTEMA', 'ti ti-coins', 'purple', 'comisiones.html', TRUE, NOW() - INTERVAL 3 HOUR);");
                System.out.println("Notificaciones iniciales insertadas con éxito.");
            }

            System.out.println("\n=== COLUMNAS ACTUALES EN NOTIFICACION ===");
            try (ResultSet rs = meta.getColumns(null, null, "Notificacion", null)) {
                while (rs.next()) {
                    System.out.println("  ✓ " + rs.getString("COLUMN_NAME") + " (" + rs.getString("TYPE_NAME") + ")");
                }
            }

            System.out.println("\n=== REGISTROS ACTUALES EN NOTIFICACION ===");
            try (ResultSet rs = stmt.executeQuery("SELECT idNotificacion, titulo, tipo, leida, fechaEnvio FROM Notificacion ORDER BY idNotificacion DESC LIMIT 5")) {
                while (rs.next()) {
                    System.out.println("  [" + rs.getInt("idNotificacion") + "] " + rs.getString("titulo") + " | Tipo: " + rs.getString("tipo") + " | Leída: " + rs.getBoolean("leida") + " | Fecha: " + rs.getString("fechaEnvio"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
