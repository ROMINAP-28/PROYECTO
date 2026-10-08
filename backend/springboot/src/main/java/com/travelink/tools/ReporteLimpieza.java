package com.travelink.tools;

import com.travelink.config.ConexionDB;
import java.sql.*;

public class ReporteLimpieza {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  REPORTE DIAGNÓSTICO PARA PASO A (DBTravelink)");
        System.out.println("=================================================");
        try (Connection con = ConexionDB.getConnection()) {
            if (con == null) {
                System.err.println("No se pudo conectar a la base de datos.");
                return;
            }

            // 1. ServicioTuristico
            System.out.println("\n--- 1. TABLA ServicioTuristico ---");
            String sqlST = "SELECT s.idServicio, s.idAgencia, s.nombre, s.descripcion FROM ServicioTuristico s";
            int countSTBasura = 0;
            int countSTOk = 0;
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlST)) {
                while (rs.next()) {
                    int id = rs.getInt("idServicio");
                    int idAg = rs.getInt("idAgencia");
                    String nom = rs.getString("nombre");
                    String desc = rs.getString("descripcion");
                    boolean esBasura = (desc != null && desc.trim().equals("-")) || nom.equalsIgnoreCase("Lima") || nom.equalsIgnoreCase("Tarapoto") || nom.equalsIgnoreCase("Camana");
                    if (esBasura) {
                        countSTBasura++;
                        System.out.printf("  [ELIMINAR - BASURA] idServicio=%d | idAgencia=%d | nombre='%s' | desc='%s'%n", id, idAg, nom, desc);
                    } else {
                        countSTOk++;
                        System.out.printf("  [CONSERVAR - REAL]   idServicio=%d | idAgencia=%d | nombre='%s'%n", id, idAg, nom);
                    }
                }
            }
            System.out.printf(">>> Total ServicioTuristico a ELIMINAR: %d | Total a CONSERVAR: %d%n", countSTBasura, countSTOk);

            // 2. Tour
            System.out.println("\n--- 2. TABLA Tour ---");
            String sqlT = "SELECT t.idTour, t.idAgencia, t.nombre FROM Tour t";
            int countTPrueba = 0;
            int countTOk = 0;
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlT)) {
                while (rs.next()) {
                    int id = rs.getInt("idTour");
                    int idAg = rs.getInt("idAgencia");
                    String nom = rs.getString("nombre");
                    boolean esPrueba = nom.toLowerCase().contains("prueba") || nom.toLowerCase().contains("verificacion");
                    if (esPrueba) {
                        countTPrueba++;
                        System.out.printf("  [ELIMINAR - PRUEBA] idTour=%d | idAgencia=%d | nombre='%s'%n", id, idAg, nom);
                    } else {
                        countTOk++;
                        System.out.printf("  [CONSERVAR - REAL]   idTour=%d | idAgencia=%d | nombre='%s'%n", id, idAg, nom);
                    }
                }
            }
            System.out.printf(">>> Total Tour a ELIMINAR: %d | Total a CONSERVAR: %d%n", countTPrueba, countTOk);

            // 3. Tour sin ServicioTuristico (para Backfill PASO B)
            System.out.println("\n--- 3. Tour sin ServicioTuristico (Para Backfill PASO B) ---");
            String sqlTNoS = "SELECT t.idTour, t.idAgencia, t.nombre FROM Tour t LEFT JOIN ServicioTuristico s ON t.idTour = s.idServicio WHERE s.idServicio IS NULL";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlTNoS)) {
                int countHuerfanos = 0;
                while (rs.next()) {
                    countHuerfanos++;
                    System.out.printf("  [HÉRANO - REQUIERE BACKFILL] idTour=%d | idAgencia=%d | nombre='%s'%n", rs.getInt("idTour"), rs.getInt("idAgencia"), rs.getString("nombre"));
                }
                System.out.printf(">>> Total Tours sin ServicioTuristico: %d%n", countHuerfanos);
            }

            // 4. PaqueteTuristico
            System.out.println("\n--- 4. TABLA PaqueteTuristico ---");
            String sqlP = "SELECT idPaquete, idAgencia, nombre FROM PaqueteTuristico";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlP)) {
                int countP = 0;
                while (rs.next()) {
                    countP++;
                    int id = rs.getInt("idPaquete");
                    String nom = rs.getString("nombre");
                    boolean esPrueba = nom.toLowerCase().contains("prueba") || nom.toLowerCase().contains("verificacion");
                    System.out.printf("  [%s] idPaquete=%d | idAgencia=%d | nombre='%s'%n", esPrueba ? "ELIMINAR" : "CONSERVAR", id, rs.getInt("idAgencia"), nom);
                }
                System.out.printf(">>> Total PaqueteTuristico: %d%n", countP);
            }

            // 5. Disponibilidad
            System.out.println("\n--- 5. TABLA Disponibilidad ---");
            String sqlDisp = "SELECT d.idDisponibilidad, d.idServicio, s.nombre, s.descripcion FROM Disponibilidad d LEFT JOIN ServicioTuristico s ON d.idServicio = s.idServicio";
            int countDispBasura = 0;
            int countDispOk = 0;
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlDisp)) {
                while (rs.next()) {
                    int idDisp = rs.getInt("idDisponibilidad");
                    int idS = rs.getInt("idServicio");
                    String nomS = rs.getString("nombre");
                    String descS = rs.getString("descripcion");
                    boolean esBasura = (nomS == null) || (descS != null && descS.trim().equals("-")) || nomS.equalsIgnoreCase("Lima") || nomS.equalsIgnoreCase("Tarapoto") || nomS.equalsIgnoreCase("Camana");
                    if (esBasura) {
                        countDispBasura++;
                        System.out.printf("  [ELIMINAR - BASURA/HUÉRFANA] idDisponibilidad=%d | idServicio=%d | servicio='%s'%n", idDisp, idS, nomS);
                    } else {
                        countDispOk++;
                        System.out.printf("  [CONSERVAR] idDisponibilidad=%d | idServicio=%d | servicio='%s'%n", idDisp, idS, nomS);
                    }
                }
            }
            System.out.printf(">>> Total Disponibilidad a ELIMINAR: %d | Total a CONSERVAR: %d%n", countDispBasura, countDispOk);

            // 6. Reservas y Pasajeros
            System.out.println("\n--- 6. TABLA Reserva y Pasajeros ---");
            String sqlRes = "SELECT r.idReserva, r.codigoReserva, r.idUsuario, r.estado FROM Reserva r";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlRes)) {
                int countR = 0;
                while (rs.next()) {
                    countR++;
                    System.out.printf("  [RESERVA] idReserva=%d | codigo='%s' | idUsuario=%d | estado='%s'%n",
                            rs.getInt("idReserva"), rs.getString("codigoReserva"), rs.getInt("idUsuario"), rs.getString("estado"));
                }
                System.out.printf(">>> Total Reservas: %d%n", countR);
            }

            // 7. Agencias y Usuarios
            System.out.println("\n--- 7. TABLA Agencia y Usuario ---");
            String sqlAg = "SELECT a.idAgencia, a.nombreComercial, a.ruc, u.idUsuario, u.nombreUsuario FROM Agencia a LEFT JOIN Usuario u ON a.idUsuario = u.idUsuario";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlAg)) {
                int countAg = 0;
                while (rs.next()) {
                    countAg++;
                    System.out.printf("  [AGENCIA] idAgencia=%d | nombre='%s' | ruc='%s' | idUsuario=%d (%s)%n",
                            rs.getInt("idAgencia"), rs.getString("nombreComercial"), rs.getString("ruc"), rs.getInt("idUsuario"), rs.getString("nombreUsuario"));
                }
                System.out.printf(">>> Total Agencias: %d%n", countAg);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
