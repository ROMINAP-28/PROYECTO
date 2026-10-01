import java.sql.*;
import java.util.*;
import com.travelink.config.ConexionDB;

public class SeedCompleteSystem {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("INICIANDO POBLAMIENTO Y PRUEBAS DEL SISTEMA TRAVELINK");
        System.out.println("==================================================");

        try (Connection conn = ConexionDB.getConnection()) {
            if (conn == null) {
                System.err.println("ERROR: No se pudo conectar a DBTravelink.");
                return;
            }
            conn.setAutoCommit(false);

            Statement stmt = conn.createStatement();

            // 1. Limpieza de reservas antiguas de prueba si existen
            System.out.println("1. Limpiando transacciones de prueba anteriores...");
            stmt.executeUpdate("DELETE FROM Calificacion");
            stmt.executeUpdate("DELETE FROM MetodoPago");
            stmt.executeUpdate("DELETE FROM Pago");
            stmt.executeUpdate("DELETE FROM Pasajero");
            stmt.executeUpdate("DELETE FROM DetalleReserva");
            stmt.executeUpdate("DELETE FROM Comision");
            stmt.executeUpdate("DELETE FROM Liquidacion");
            stmt.executeUpdate("DELETE FROM Reserva");

            // 2. Asegurar Roles básicos
            System.out.println("2. Verificando roles...");
            stmt.executeUpdate("INSERT IGNORE INTO Rol (idRol, nombreRol) VALUES (1, 'Turista'), (2, 'Agencia'), (3, 'Administrador')");

            // 3. Crear 10 Turistas con Persona + Usuario
            System.out.println("3. Creando 10 Turistas de prueba con sus cuentas...");
            String[][] turistasData = {
                {"Carlos Manuel", "Rojas", "Vargas", "72019283", "981234567", "carlos.rojas@travelink.test", "turista01", "pass123"},
                {"Maria Elena", "Sánchez", "Gómez", "73192834", "982345678", "maria.sanchez@travelink.test", "turista02", "pass123"},
                {"Jorge Luis", "Fernández", "Pérez", "74283940", "983456789", "jorge.fernandez@travelink.test", "turista03", "pass123"},
                {"Ana Lucía", "Morales", "Castro", "75394051", "984567890", "ana.morales@travelink.test", "turista04", "pass123"},
                {"Diego Armando", "Quispe", "Mamani", "76405162", "985678901", "diego.quispe@travelink.test", "turista05", "pass123"},
                {"Sofia Isabel", "Ramírez", "Díaz", "77516273", "986789012", "sofia.ramirez@travelink.test", "turista06", "pass123"},
                {"Mateo Gabriel", "Chávez", "Flores", "78627384", "987890123", "mateo.chavez@travelink.test", "turista07", "pass123"},
                {"Camila Andrea", "Vásquez", "Espinoza", "79738495", "988901234", "camila.vasquez@travelink.test", "turista08", "pass123"},
                {"Renzo Fernando", "Torres", "Guerrero", "70849506", "989012345", "renzo.torres@travelink.test", "turista09", "pass123"},
                {"Luciana Beatriz", "Mendoza", "Romero", "71950617", "990123456", "luciana.mendoza@travelink.test", "turista10", "pass123"}
            };

            int[] turistaUsuarioIds = new int[10];

            for (int i = 0; i < turistasData.length; i++) {
                String[] t = turistasData[i];
                // Insert Persona
                PreparedStatement psPersona = conn.prepareStatement(
                    "INSERT INTO Persona (nombre, apellidoPaterno, apellidoMaterno, nroDocumento, telefono, email) " +
                    "VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE idPersona=LAST_INSERT_ID(idPersona)",
                    Statement.RETURN_GENERATED_KEYS
                );
                psPersona.setString(1, t[0]);
                psPersona.setString(2, t[1]);
                psPersona.setString(3, t[2]);
                psPersona.setString(4, t[3]);
                psPersona.setString(5, t[4]);
                psPersona.setString(6, t[5]);
                psPersona.executeUpdate();

                int idPersona = 0;
                ResultSet rs = psPersona.getGeneratedKeys();
                if (rs.next()) idPersona = rs.getInt(1);

                // Insert Usuario
                PreparedStatement psUser = conn.prepareStatement(
                    "INSERT INTO Usuario (idPersona, idRol, nombreUsuario, contrasena, estado) " +
                    "VALUES (?, 1, ?, ?, 'ACTIVO') ON DUPLICATE KEY UPDATE idUsuario=LAST_INSERT_ID(idUsuario)",
                    Statement.RETURN_GENERATED_KEYS
                );
                psUser.setInt(1, idPersona);
                psUser.setString(2, t[6]);
                psUser.setString(3, t[7]);
                psUser.executeUpdate();

                ResultSet rsU = psUser.getGeneratedKeys();
                if (rsU.next()) {
                    turistaUsuarioIds[i] = rsU.getInt(1);
                } else {
                    // query fallback
                    PreparedStatement psFind = conn.prepareStatement("SELECT idUsuario FROM Usuario WHERE nombreUsuario = ?");
                    psFind.setString(1, t[6]);
                    ResultSet rsF = psFind.executeQuery();
                    if (rsF.next()) turistaUsuarioIds[i] = rsF.getInt(1);
                }
            }
            System.out.println("-> 10 Turistas insertados/verificados correctamente.");

            // 4. Asegurar las 6 Agencias en estado ACTIVO y crear solicitudes pendientes/rechazadas para prueba
            System.out.println("4. Actualizando agencias y creando solicitudes postulantes con motivos...");
            stmt.executeUpdate("UPDATE Agencia SET estado = 'ACTIVO' WHERE idAgencia IN (1, 2, 3, 5, 6, 7)");

            // Agregar solicitudes pendientes y rechazadas
            stmt.executeUpdate("DELETE FROM SolicitudAgencia WHERE ruc IN ('20998877661', '20887766552', '20776655443')");

            // Postulante 1: Pendiente
            PreparedStatement psSol1 = conn.prepareStatement(
                "INSERT INTO SolicitudAgencia (idUsuario, razonSocial, ruc, representanteLegal, telefonoContacto, correoContacto, documentoRuc, estado) " +
                "VALUES (?, 'PERU PACIFIC TOURS E.I.R.L.', '20998877661', 'Hernán Castillo Silva', '941238901', 'contacto@pacifictours.pe', 'ficha_ruc_pacific.pdf', 'Pendiente')"
            );
            psSol1.setInt(1, turistaUsuarioIds[0]);
            psSol1.executeUpdate();

            // Postulante 2: Rechazada con motivo
            PreparedStatement psSol2 = conn.prepareStatement(
                "INSERT INTO SolicitudAgencia (idUsuario, razonSocial, ruc, representanteLegal, telefonoContacto, correoContacto, documentoRuc, estado, fechaRevision, motivoRechazo) " +
                "VALUES (?, 'CHACHAPOYAS ADVENTURES S.A.C.', '20887766552', 'Sonia Alarcón Vega', '952349012', 'informes@chachapoyasadv.pe', 'ruc_chachapoyas.pdf', 'Rechazado', NOW(), 'Falta de certificado MINCETUR actualizado y seguro SOAT turístico vencido')"
            );
            psSol2.setInt(1, turistaUsuarioIds[1]);
            psSol2.executeUpdate();

            // Postulante 3: Rechazada con motivo
            PreparedStatement psSol3 = conn.prepareStatement(
                "INSERT INTO SolicitudAgencia (idUsuario, razonSocial, ruc, representanteLegal, telefonoContacto, correoContacto, documentoRuc, estado, fechaRevision, motivoRechazo) " +
                "VALUES (?, 'VALLE SAGRADO EXPEDITIONS', '20776655443', 'Gonzalo Beltrán Rios', '963450123', 'reservas@vallesagradoexp.com', 'ruc_valle.pdf', 'Rechazado', NOW(), 'Dirección fiscal declarada no coincide con el domicilio registrado en SUNAT')"
            );
            psSol3.setInt(1, turistaUsuarioIds[2]);
            psSol3.executeUpdate();

            // 5. Crear 15+ Reservas variadas para los 10 turistas distribuidas entre las 6 agencias
            System.out.println("5. Generando 15 reservas distribuidas entre los 10 turistas y 6 agencias...");

            Object[][] reservasSpec = {
                // {idTuristaIdx, idAgencia, nombreTour, total, cantAdultos, cantNinos, cantBebes, metodoPago, nroOp, estrellas, comentario}
                {0, 1, "Ruta Heroica y Valles del Pisco", 890.00, 2, 0, 0, "Yape", "OP-1001", 5, "¡Increíble servicio y atención de primera en Tacna y Moquegua!"},
                {0, 2, "Valle Sagrado de los Incas", 640.00, 2, 0, 0, "Tarjeta", "OP-1002", 4, "Muy buen recorrido guiado por el Valle Sagrado."},
                {0, 7, "City Tour Lima Colonial y Catacumbas", 245.00, 2, 1, 0, "Transferencia", "OP-1003", 5, "Fascinante recorrido por las catacumbas de Lima."},

                {1, 1, "Amazonía Profunda Tambopata", 1450.00, 1, 0, 0, "Tarjeta", "OP-1004", 5, "Inolvidable experiencia en la selva de Madre de Dios."},

                {2, 3, "Expedición Bosque de Piedras de Huayllay", 720.00, 2, 1, 1, "Yape", "OP-1005", 4, "El bosque de piedras fue espectacular, los niños lo disfrutaron mucho."},
                {2, 5, "Aventura Marina en Reserva Punta de Coles Ilo", 375.00, 2, 1, 0, "Tarjeta", "OP-1006", 5, "Excelente avistamiento de lobos marinos y pingüinos en Ilo."},

                {3, 6, "Circuito Valle Viejo y Viñedos de Pocollay", 260.00, 2, 1, 0, "PagoEfectivo", "OP-1007", 5, "Los vinos y el macerado de damasco riquísimos. Súper recomendado."},
                {3, 7, "Ruta Gastronómica y Bohemia en Barranco", 285.00, 2, 0, 1, "Yape", "OP-1008", 4, "La comida criolla deliciosa y el café de Barranco excelente."},

                {4, 1, "Mar y Valle del Sur (Ilo + Moquegua)", 680.00, 1, 0, 0, "Transferencia", "OP-1009", 5, "Hermosa combinación de playa y campiña."},

                {5, 2, "Valle Sagrado de los Incas", 860.00, 2, 2, 0, "Tarjeta", "OP-1010", 4, "Tours puntuales y hospedajes de buena categoría."},
                {5, 5, "Aventura Marina en Reserva Punta de Coles Ilo", 290.00, 2, 0, 0, "Yape", "OP-1011", 5, "Muy buena atención del guía marino."},

                {6, 3, "Expedición Bosque de Piedras de Huayllay", 480.00, 3, 0, 0, "Tarjeta", "OP-1012", 5, "Una caminata hermosa a más de 4000 msnm."},

                {7, 6, "Ruta Colonial del Vino y Campiñas de Moquegua", 310.00, 2, 1, 0, "Yape", "OP-1013", 4, "Tradición y arquitectura única en Moquegua."},

                {8, 7, "Circuito Costero Playas de Ilo y Malecón", 230.00, 2, 1, 0, "Transferencia", "OP-1014", 5, "Las playas de Ilo son limpias y apacibles."},
                {8, 1, "Expedición Andina y Bosque de Piedras", 720.00, 1, 0, 0, "Tarjeta", "OP-1015", 5, "Todo genial con Andes Tours."}
            };

            for (int k = 0; k < reservasSpec.length; k++) {
                Object[] r = reservasSpec[k];
                int turistaIdx = (Integer) r[0];
                int idUsuario = turistaUsuarioIds[turistaIdx];
                int idAgencia = (Integer) r[1];
                String nombreTour = (String) r[2];
                double total = (Double) r[3];
                int adultos = (Integer) r[4];
                int ninos = (Integer) r[5];
                int bebes = (Integer) r[6];
                String metodo = (String) r[7];
                String nroOp = (String) r[8];
                int estrellas = (Integer) r[9];
                String comentario = (String) r[10];

                String codReserva = "TRK-2026-" + String.format("%03d", (k + 1));

                // Insert Reserva
                PreparedStatement psRes = conn.prepareStatement(
                    "INSERT INTO Reserva (idUsuario, idAgencia, nombreTour, codigoReserva, fechaInicio, fechaFin, estado, total) " +
                    "VALUES (?, ?, ?, ?, DATE_ADD(CURRENT_DATE, INTERVAL ? DAY), DATE_ADD(CURRENT_DATE, INTERVAL ? DAY), 'CONFIRMADA', ?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                psRes.setInt(1, idUsuario);
                psRes.setInt(2, idAgencia);
                psRes.setString(3, nombreTour);
                psRes.setString(4, codReserva);
                psRes.setInt(5, (k + 1) * 2);
                psRes.setInt(6, (k + 1) * 2 + 2);
                psRes.setDouble(7, total);
                psRes.executeUpdate();

                ResultSet rsRes = psRes.getGeneratedKeys();
                int idReserva = 0;
                if (rsRes.next()) idReserva = rsRes.getInt(1);

                // Query exact Persona details for current turistaUsuario
                PreparedStatement psFindPersona = conn.prepareStatement(
                    "SELECT p.nroDocumento, p.nombre, p.apellidoPaterno, p.apellidoMaterno, p.telefono " +
                    "FROM Persona p JOIN Usuario u ON p.idPersona = u.idPersona WHERE u.idUsuario = ?"
                );
                psFindPersona.setInt(1, idUsuario);
                ResultSet rsP = psFindPersona.executeQuery();
                String pDni = "72019283";
                String pNombre = "Carlos Manuel";
                String pPaterno = "Rojas";
                String pMaterno = "Vargas";
                String pTelefono = "981234567";
                if (rsP.next()) {
                    pDni = rsP.getString("nroDocumento");
                    pNombre = rsP.getString("nombre");
                    pPaterno = rsP.getString("apellidoPaterno");
                    pMaterno = rsP.getString("apellidoMaterno");
                    pTelefono = rsP.getString("telefono");
                }
                String pApellidos = pPaterno + " " + pMaterno;

                // Companion name arrays per tourist family
                String[][] acompañantesAdultos = {
                    {"Laura Patricia", pPaterno, "Morales", "72948102"},
                    {"Eduardo José", pPaterno, "Alvarado", "73819204"},
                    {"Gabriel Alonso", pPaterno, "Castillo", "74920183"}
                };

                String[][] acompañantesNinos = {
                    {"Diego Mateo", pPaterno, "Morales", "61928374", "8"},
                    {"Valentina Sofía", pPaterno, "Morales", "62019283", "6"},
                    {"Luciana Isabel", pPaterno, "Castillo", "63102938", "10"}
                };

                String[][] acompañantesBebes = {
                    {"Thiago Benjamín", pPaterno, "Morales", "51920384", "1"},
                    {"Emma Lucía", pPaterno, "Castillo", "52019283", "1"}
                };

                // Insert Pasajeros
                for (int a = 1; a <= adultos; a++) {
                    PreparedStatement psPas = conn.prepareStatement(
                        "INSERT INTO Pasajero (idReserva, nroDocumento, telefono, nombre, apellidoPaterno, apellidoMaterno, apellidos, esTitular) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                    );
                    psPas.setInt(1, idReserva);
                    if (a == 1) {
                        // Titular matches Persona EXACTLY
                        psPas.setString(2, pDni);
                        psPas.setString(3, pTelefono);
                        psPas.setString(4, pNombre);
                        psPas.setString(5, pPaterno);
                        psPas.setString(6, pMaterno);
                        psPas.setString(7, pApellidos);
                        psPas.setBoolean(8, true);
                    } else {
                        // Companion adult
                        String[] ac = acompañantesAdultos[(a - 2) % acompañantesAdultos.length];
                        psPas.setString(2, ac[3]);
                        psPas.setString(3, pTelefono);
                        psPas.setString(4, ac[0]);
                        psPas.setString(5, ac[1]);
                        psPas.setString(6, ac[2]);
                        psPas.setString(7, ac[1] + " " + ac[2]);
                        psPas.setBoolean(8, false);
                    }
                    psPas.executeUpdate();
                }

                if (ninos > 0) {
                    for (int n = 1; n <= ninos; n++) {
                        String[] cn = acompañantesNinos[(n - 1) % acompañantesNinos.length];
                        PreparedStatement psPas = conn.prepareStatement(
                            "INSERT INTO Pasajero (idReserva, nroDocumento, telefono, nombre, apellidoPaterno, apellidoMaterno, apellidos, esTitular) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, false)"
                        );
                        psPas.setInt(1, idReserva);
                        psPas.setString(2, cn[3]);
                        psPas.setString(3, pTelefono);
                        psPas.setString(4, cn[0]);
                        psPas.setString(5, cn[1]);
                        psPas.setString(6, cn[2]);
                        psPas.setString(7, cn[1] + " " + cn[2]);
                        psPas.executeUpdate();
                    }
                }

                if (bebes > 0) {
                    for (int b = 1; b <= bebes; b++) {
                        String[] cb = acompañantesBebes[(b - 1) % acompañantesBebes.length];
                        PreparedStatement psPas = conn.prepareStatement(
                            "INSERT INTO Pasajero (idReserva, nroDocumento, telefono, nombre, apellidoPaterno, apellidoMaterno, apellidos, esTitular) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, false)"
                        );
                        psPas.setInt(1, idReserva);
                        psPas.setString(2, cb[3]);
                        psPas.setString(3, pTelefono);
                        psPas.setString(4, cb[0]);
                        psPas.setString(5, cb[1]);
                        psPas.setString(6, cb[2]);
                        psPas.setString(7, cb[1] + " " + cb[2]);
                        psPas.executeUpdate();
                    }
                }

                // Insert Pago & MetodoPago
                PreparedStatement psPago = conn.prepareStatement(
                    "INSERT INTO Pago (idReserva, monto, metodoPago, estado, numeroOperacion) VALUES (?, ?, ?, 'COMPLETADO', ?)"
                );
                psPago.setInt(1, idReserva);
                psPago.setDouble(2, total);
                psPago.setString(3, metodo);
                psPago.setString(4, nroOp);
                psPago.executeUpdate();

                PreparedStatement psMetodo = conn.prepareStatement(
                    "INSERT INTO MetodoPago (idReserva, tipoPago, monto, esAdelanto, estadoPago, numeroOperacion) VALUES (?, ?, ?, false, 'Completado', ?)"
                );
                psMetodo.setInt(1, idReserva);
                psMetodo.setString(2, metodo);
                psMetodo.setDouble(3, total);
                psMetodo.setString(4, nroOp);
                psMetodo.executeUpdate();

                // Insert Calificación
                PreparedStatement psCal = conn.prepareStatement(
                    "INSERT INTO Calificacion (idUsuario, idAgencia, idReserva, estrellas, comentario) VALUES (?, ?, ?, ?, ?)"
                );
                psCal.setInt(1, idUsuario);
                psCal.setInt(2, idAgencia);
                psCal.setInt(3, idReserva);
                psCal.setInt(4, estrellas);
                psCal.setString(5, comentario);
                psCal.executeUpdate();

                // Insert Comisión 15%
                double montoComision = total * 0.15;
                double montoNeto = total - montoComision;
                PreparedStatement psCom = conn.prepareStatement(
                    "INSERT INTO Comision (idAgencia, idReserva, subtotal, porcentajeComision, montoComision, montoAgencia, estadoLiquidacion) VALUES (?, ?, ?, 15.00, ?, ?, 'Pendiente')"
                );
                psCom.setInt(1, idAgencia);
                psCom.setInt(2, idReserva);
                psCom.setDouble(3, total);
                psCom.setDouble(4, montoComision);
                psCom.setDouble(5, montoNeto);
                psCom.executeUpdate();
            }
            System.out.println("-> 15 Reservas con pasajeros, pagos, calificaciones y comisiones insertadas.");

            // 6. Recalcular y Actualizar Promedio de Calificación por Agencia
            System.out.println("6. Recalculando y actualizando promedio de calificaciones reales por Agencia...");
            String sqlPromedios = 
                "UPDATE Agencia a " +
                "SET a.promedioCalificacion = COALESCE((" +
                "    SELECT ROUND(AVG(c.estrellas), 2) " +
                "    FROM Calificacion c " +
                "    WHERE c.idAgencia = a.idAgencia" +
                "), 5.00)";
            stmt.executeUpdate(sqlPromedios);

            // 7. Notificaciones del sistema
            System.out.println("7. Generando notificaciones reales...");
            stmt.executeUpdate("DELETE FROM Notificacion");
            stmt.executeUpdate(
                "INSERT INTO Notificacion (idUsuario, titulo, mensaje, tipo, estado, icono, color) VALUES " +
                "(6, 'Nueva Solicitud de Agencia', 'La agencia PERU PACIFIC TOURS E.I.R.L. ha enviado su expediente para evaluación.', 'AGENCIA', 'ENVIADA', 'ti ti-building-store', 'blue'), " +
                "(6, 'Solicitud Rechazada con Motivo', 'Se registró el rechazo formal de CHACHAPOYAS ADVENTURES por SOAT vencido.', 'AGENCIA', 'ENVIADA', 'ti ti-alert-triangle', 'red'), " +
                "(2, 'Nueva Reserva Recibida', 'Reserva TRK-2026-001 confirmada por S/ 890.00 para la Ruta Heroica.', 'RESERVA', 'ENVIADA', 'ti ti-calendar-event', 'green'), " +
                "(1, 'Reserva Confirmada', 'Tu reserva TRK-2026-001 fue registrada exitosamente con pago completado.', 'RESERVA', 'ENVIADA', 'ti ti-circle-check', 'green')"
            );

            conn.commit();
            System.out.println("==================================================");
            System.out.println("POBLAMIENTO Y PRUEBA DE BASE DE DATOS COMPLETADO CON ÉXITO!");
            System.out.println("==================================================");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
