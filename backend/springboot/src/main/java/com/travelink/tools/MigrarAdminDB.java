package com.travelink.tools;

import com.travelink.config.ConexionDB;
import java.sql.*;

public class MigrarAdminDB {
    public static void main(String[] args) {
        System.out.println("=== CONFIGURANDO TABLAS Y SEED PARA ADMINISTRADOR ===");
        try (Connection con = ConexionDB.getConnection()) {
            if (con == null) {
                System.err.println("No hay conexion a la base de datos.");
                return;
            }

            try (Statement st = con.createStatement()) {
                // Adaptar columnas en Comision si faltan
                ejecutarSqlSilencioso(st, "ALTER TABLE Comision ADD COLUMN idAgencia INT NULL");
                ejecutarSqlSilencioso(st, "ALTER TABLE Comision ADD COLUMN subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00");
                ejecutarSqlSilencioso(st, "ALTER TABLE Comision ADD COLUMN porcentajeComision DECIMAL(4,2) NOT NULL DEFAULT 10.00");
                ejecutarSqlSilencioso(st, "ALTER TABLE Comision ADD COLUMN montoAgencia DECIMAL(10,2) NOT NULL DEFAULT 0.00");
                ejecutarSqlSilencioso(st, "ALTER TABLE Comision ADD COLUMN estadoLiquidacion VARCHAR(20) NOT NULL DEFAULT 'Pendiente'");
                ejecutarSqlSilencioso(st, "ALTER TABLE Comision ADD COLUMN fechaLiquidacion DATETIME NULL");

                // Semillas de SolicitudAgencia
                st.execute("INSERT IGNORE INTO SolicitudAgencia (idSolicitud, idUsuario, razonSocial, ruc, representanteLegal, telefonoContacto, correoContacto, documentoRuc, fechaSolicitud, estado) VALUES " +
                        "(1, 2, 'PERUVIAN ADVENTURES S.A.C.', '20556677889', 'Raul Espinoza Gomez', '944112233', 'contacto@peruvianadv.pe', 'ficha_ruc_20556677889.pdf', NOW() - INTERVAL 2 HOUR, 'Pendiente'), " +
                        "(2, 3, 'KUNTUR EXPEDITIONS E.I.R.L.', '20448899112', 'Maria Fernandez Cardenas', '988776655', 'reservas@kunturexpeditions.com', 'ruc_kuntur_2026.pdf', NOW() - INTERVAL 1 DAY, 'Pendiente'), " +
                        "(3, 2, 'AMAZON JUNGLE TOURS S.A.', '20112233445', 'Jorge Ramos Silva', '977441188', 'info@amazonjungle.pe', 'ruc_amazon.pdf', NOW() - INTERVAL 3 DAY, 'Pendiente');");

                // Semillas de HistorialAgencia
                st.execute("INSERT IGNORE INTO HistorialAgencia (idHistorial, idAgencia, idAdministrador, estadoAnterior, estadoNuevo, motivo, fecha) VALUES " +
                        "(1, 1, 6, 'Pendiente', 'ACTIVO', 'Aprobación inicial de incorporación con documentación validada', NOW() - INTERVAL 20 DAY), " +
                        "(2, 2, 6, 'Pendiente', 'ACTIVO', 'Registro y validación de RUC Sunat conforme', NOW() - INTERVAL 15 DAY);");

                // Semillas de Comisiones
                st.execute("INSERT IGNORE INTO Comision (idComision, idAgencia, subtotal, porcentajeComision, montoComision, montoAgencia, estadoLiquidacion, fechaLiquidacion, fechaCalculo) VALUES " +
                        "(1, 1, 1200.00, 10.00, 120.00, 1080.00, 'Pendiente', NULL, NOW() - INTERVAL 2 DAY), " +
                        "(2, 1, 3500.00, 10.00, 350.00, 3150.00, 'Pendiente', NULL, NOW() - INTERVAL 5 DAY), " +
                        "(3, 2, 2400.00, 12.00, 288.00, 2112.00, 'Pendiente', NULL, NOW() - INTERVAL 1 DAY), " +
                        "(4, 2, 1800.00, 10.00, 180.00, 1620.00, 'Liquidado', NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 20 DAY), " +
                        "(5, 1, 4200.00, 10.00, 420.00, 3780.00, 'Liquidado', NOW() - INTERVAL 35 DAY, NOW() - INTERVAL 40 DAY), " +
                        "(6, 2, 3100.00, 10.00, 310.00, 2790.00, 'Liquidado', NOW() - INTERVAL 65 DAY, NOW() - INTERVAL 70 DAY), " +
                        "(7, 1, 5100.00, 10.00, 510.00, 4590.00, 'Liquidado', NOW() - INTERVAL 95 DAY, NOW() - INTERVAL 100 DAY), " +
                        "(8, 2, 4500.00, 10.00, 450.00, 4050.00, 'Liquidado', NOW() - INTERVAL 125 DAY, NOW() - INTERVAL 130 DAY), " +
                        "(9, 1, 3800.00, 10.00, 380.00, 3420.00, 'Liquidado', NOW() - INTERVAL 155 DAY, NOW() - INTERVAL 160 DAY), " +
                        "(10, 2, 2900.00, 10.00, 290.00, 2610.00, 'Liquidado', NOW() - INTERVAL 185 DAY, NOW() - INTERVAL 190 DAY);");

                // Semillas de Calificaciones
                st.execute("INSERT IGNORE INTO Calificacion (idCalificacion, idUsuario, idAgencia, idReserva, estrellas, comentario, eliminadoPorAdmin, fechaCalificacion) VALUES " +
                        "(2, 1, 2, 1, 1, 'Pésimo servicio, el guía nunca llegó a la hora pactada y no respondieron las llamadas.', FALSE, NOW() - INTERVAL 1 DAY), " +
                        "(3, 4, 1, 1, 2, 'El transporte estaba en malas condiciones y el recorrido fue recortado.', FALSE, NOW() - INTERVAL 3 DAY), " +
                        "(4, 1, 1, 1, 5, 'Todo excelente, servicio 100% recomendado.', FALSE, NOW() - INTERVAL 10 DAY);");

                // Actualizar agencias y tours
                st.execute("UPDATE Agencia SET promedioCalificacion = 4.8, porcentajeComision = 10.00, estado = 'ACTIVO' WHERE idAgencia = 1;");
                st.execute("UPDATE Agencia SET promedioCalificacion = 3.2, porcentajeComision = 12.00, estado = 'ACTIVO' WHERE idAgencia = 2;");
                st.execute("UPDATE Tour SET estado = 'ACTIVO' WHERE estado IS NULL OR estado = '';");

                System.out.println("  ✓ Tablas y semillas de Admin configuradas correctamente.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void ejecutarSqlSilencioso(Statement st, String sql) {
        try {
            st.execute(sql);
            System.out.println("  ✓ Ejecutado: " + sql);
        } catch (SQLException e) {
            System.out.println("  - Info: " + e.getMessage());
        }
    }
}
