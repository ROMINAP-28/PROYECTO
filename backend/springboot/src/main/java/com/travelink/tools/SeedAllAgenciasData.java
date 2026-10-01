package com.travelink.tools;

import com.travelink.config.ConexionDB;
import java.sql.*;

public class SeedAllAgenciasData {

    public static void main(String[] args) {
        try (Connection conn = ConexionDB.getConnection()) {
            if (conn == null) {
                System.err.println("No se pudo conectar a la base de datos.");
                return;
            }

            conn.setAutoCommit(false);

            System.out.println("=== 1. VERIFICANDO Y ACTUALIZANDO CREDENCIALES DE LAS 6 AGENCIAS ===");
            int[][] agenciasUsuarios = {
                {1, 2},  // ANDES TOURS
                {2, 3},  // INKA TRAVEL
                {3, 5},  // AGENCIAS ALEGRIA S.A
                {5, 8},  // AGENCIA SELVA S.A
                {6, 9},  // TOUR AREQUIPA
                {7, 10}  // TOUR LIMA
            };

            for (int[] au : agenciasUsuarios) {
                int idAgencia = au[0];
                int idUsuario = au[1];
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Agencia SET idUsuario = ?, estado = 'ACTIVO' WHERE idAgencia = ?")) {
                    ps.setInt(1, idUsuario);
                    ps.setInt(2, idAgencia);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Usuario SET contrasena = '123456', estado = 'ACTIVO' WHERE idUsuario = ?")) {
                    ps.setInt(1, idUsuario);
                    ps.executeUpdate();
                }
            }

            System.out.println("=== 2. DISTRIBUYENDO Y CREANDO SERVICIOS / TOURS PARA CADA AGENCIA ===");

            // Datos de tours específicos para cada agencia:
            Object[][] toursData = {
                // AGENCIA 1: ANDES TOURS (Cusco & Arequipa)
                {1, "Camino Inca Clásico 4D/3N", "Trek legendario por el Qhapaq Ñan hacia la ciudadela sagrada de Machu Picchu.", "Aventura", 1, 850.0, 550.0, 0.0, "4 días", "https://images.unsplash.com/photo-1526392060635-9d6019884377?w=600"},
                {1, "Tour Maras Moray y Salineras", "Visita los andenes circulares concéntricos de Moray y las impresionantes pozas de sal de Maras.", "Cultural", 1, 120.0, 70.0, 0.0, "5 horas", "https://images.unsplash.com/photo-1589802829985-817e51171b92?w=600"},
                {1, "Expedición Andina Cusco Arqueológico", "Recorrido por Sacsayhuamán, Qenqo, Puka Pukara y Tambomachay.", "Cultural", 1, 95.0, 55.0, 0.0, "4 horas", "https://images.unsplash.com/photo-1587595431973-160d0d94add1?w=600"},
                {1, "Valle Sur Tipón y Pikillacta", "Descubre la hidráulica inca en Tipón y la arquitectura pre-inca Wari en Pikillacta.", "Cultural", 1, 110.0, 65.0, 0.0, "6 horas", "https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=600"},

                // AGENCIA 2: INKA TRAVEL (Cusco)
                {2, "Valle Sagrado de los Incas", "Pisac, Ollantaytambo y Chinchero con almuerzo buffet tradicional andino.", "Cultural", 1, 320.0, 220.0, 90.0, "1 día", "https://images.unsplash.com/photo-1509299349698-dd22323b5963?w=600"},
                {2, "Machu Picchu Mágico Full Day", "Viaje panorámico en tren Expedition o Voyager y visita guiada a la maravilla del mundo.", "Cultural", 1, 650.0, 420.0, 0.0, "1 día", "https://images.unsplash.com/photo-1526392060635-9d6019884377?w=600"},
                {2, "Montaña de 7 Colores Vinicunca", "Caminata de alta montaña hacia la cumbre arcoíris con vistas del nevado Ausangate.", "Aventura", 1, 180.0, 110.0, 0.0, "1 día", "https://images.unsplash.com/photo-1589802829985-817e51171b92?w=600"},
                {2, "Laguna Humantay Aventura & Trek", "Excursión a la laguna turquesa a los pies del imponente glaciar Salkantay.", "Naturaleza", 1, 160.0, 95.0, 0.0, "1 día", "https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=600"},

                // AGENCIA 3: AGENCIAS ALEGRIA S.A (Pasco)
                {3, "Expedición Bosque de Piedras de Huayllay", "Caminata entre figuras rocosas milenarias a más de 4000 msnm y baños termales de La Calera.", "Aventura", 3, 160.0, 95.0, 0.0, "1 día", "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600"},
                {3, "Laguna Punrun y Rutas Andinas de Pasco", "Navegación en bote por la inmensa laguna Punrun con avistamiento de aves altoandinas.", "Naturaleza", 3, 140.0, 80.0, 0.0, "7 horas", "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600"},
                {3, "Tour Baños Termales y Cascada La Calera", "Circuito terapéutico en aguas termominerales rodeado de imponentes paisajes andinos.", "Descanso", 3, 85.0, 50.0, 0.0, "5 horas", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600"},
                {3, "Ruta Arqueológica Huarautambo", "Visita al puente inca y complejo arqueológico de la cultura Yarush.", "Cultural", 3, 110.0, 65.0, 0.0, "6 horas", "https://images.unsplash.com/photo-1587595431973-160d0d94add1?w=600"},

                // AGENCIA 5: AGENCIA SELVA S.A (Madre de Dios)
                {5, "Inmersión Reserva Tambopata y Lago Sandoval", "Expedición en bote por el río Madre de Dios, lobos gigantes de río, perezosos y caimanes.", "Naturaleza", 6, 290.0, 180.0, 0.0, "2 días", "https://images.unsplash.com/photo-1516426122078-c23e76319801?w=600"},
                {5, "Aventura Río Madre de Dios y Collpa de Loros", "Canopy walkway sobre la selva a 35m de altura y espectáculo matutino de guacamayos.", "Aventura", 6, 230.0, 140.0, 0.0, "8 horas", "https://images.unsplash.com/photo-1534567153574-2b12153a87f0?w=600"},
                {5, "Tour Nocturno Caimanes y Selva Virgen", "Safari fluvial nocturno en canoa motorizada buscando caimanes blancos y capibaras.", "Aventura", 6, 150.0, 90.0, 0.0, "4 horas", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600"},
                {5, "Canopy & Kayak en Río Madre de Dios", "Circuito de aventura en tirolesas amazónicas y remo por afluentes selváticos.", "Aventura", 6, 180.0, 110.0, 0.0, "6 horas", "https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=600"},

                // AGENCIA 6: TOUR AREQUIPA (Tacna / Moquegua / Arequipa)
                {6, "Circuito Valle Viejo y Viñedos de Pocollay", "Paseo campestre, casonas coloniales, degustación de piscos tacneños y macerados de damasco.", "Cultural", 7, 100.0, 60.0, 0.0, "5 horas", "https://images.unsplash.com/photo-1589802829985-817e51171b92?w=600"},
                {6, "Ruta Heroica y Aguas Termales de Calientes Tacna", "Campo del Alto de la Alianza, petroglifos de Miculla y pozas termales medicinales.", "Descanso", 7, 130.0, 75.0, 0.0, "6 horas", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600"},
                {6, "Ruta Colonial del Vino y Campiñas de Moquegua", "Bodegas pisqueras coloniales, cata de vinos y casonas históricas con techos de mojinete.", "Cultural", 4, 120.0, 70.0, 0.0, "5 horas", "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=600"},
                {6, "Expedición Valle de Torata y Cataratas de Mollesaja", "Molinos coloniales, repostería artesanal moqueguana y hermosa cascada en valle fértil.", "Naturaleza", 4, 135.0, 80.0, 0.0, "7 horas", "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600"},

                // AGENCIA 7: TOUR LIMA (Lima & Ilo)
                {7, "City Tour Lima Colonial y Catacumbas", "Plaza Mayor, Palacio de Gobierno, Catedral y criptas subterráneas de San Francisco.", "Cultural", 2, 95.0, 55.0, 0.0, "5 horas", "https://images.unsplash.com/photo-1587595431973-160d0d94add1?w=600"},
                {7, "Ruta Gastronómica y Bohemia en Barranco", "Puente de los Suspiros, Bajada de Baños y cata de cafés de especialidad y comida criolla.", "Descanso", 2, 110.0, 65.0, 0.0, "4 horas", "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=600"},
                {7, "Circuito Costero Playas de Ilo y Malecón", "Playas Puerto Inglés y Pozo de Lis, Glorieta José Gálvez y ceviche en el puerto.", "Descanso", 5, 90.0, 50.0, 0.0, "6 horas", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600"},
                {7, "Aventura Marina en Reserva Punta de Coles Ilo", "Lancha rápida hacia la reserva marina, avistamiento de cientos de lobos marinos y pingüinos.", "Aventura", 5, 145.0, 85.0, 0.0, "5 horas", "https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=600"}
            };

            for (Object[] t : toursData) {
                int idAgencia = (int) t[0];
                String nombre = (String) t[1];
                String desc = (String) t[2];
                String categoria = (String) t[3];
                int idDestino = (int) t[4];
                double precioAd = (double) t[5];
                double precioNi = (double) t[6];
                double precioBe = (double) t[7];
                String duracion = (String) t[8];
                String img = (String) t[9];
                String slug = nombre.toLowerCase().replaceAll("[^a-z0-9]+", "-");

                int idTourExistente = 0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT idTour FROM Tour WHERE idAgencia = ? AND nombre = ?")) {
                    ps.setInt(1, idAgencia);
                    ps.setString(2, nombre);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) idTourExistente = rs.getInt("idTour");
                    }
                }

                if (idTourExistente == 0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT idTour FROM Tour WHERE nombre = ?")) {
                        ps.setString(1, nombre);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                idTourExistente = rs.getInt("idTour");
                                try (PreparedStatement psUp = conn.prepareStatement(
                                        "UPDATE Tour SET idAgencia = ?, slug = ?, descripcion = ?, categoria = ?, idDestino = ?, precioAdulto = ?, precioNino = ?, precioBebe = ?, duracion = ?, estado = 'ACTIVO' WHERE idTour = ?")) {
                                    psUp.setInt(1, idAgencia);
                                    psUp.setString(2, slug);
                                    psUp.setString(3, desc);
                                    psUp.setString(4, categoria);
                                    psUp.setInt(5, idDestino);
                                    psUp.setDouble(6, precioAd);
                                    psUp.setDouble(7, precioNi);
                                    psUp.setDouble(8, precioBe);
                                    psUp.setString(9, duracion);
                                    psUp.setInt(10, idTourExistente);
                                    psUp.executeUpdate();
                                }
                            }
                        }
                    }
                }

                if (idTourExistente == 0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO Tour (idAgencia, slug, nombre, descripcion, categoria, idDestino, precioAdulto, precioNino, precioBebe, duracion, estado) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVO')", Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, idAgencia);
                        ps.setString(2, slug);
                        ps.setString(3, nombre);
                        ps.setString(4, desc);
                        ps.setString(5, categoria);
                        ps.setInt(6, idDestino);
                        ps.setDouble(7, precioAd);
                        ps.setDouble(8, precioNi);
                        ps.setDouble(9, precioBe);
                        ps.setString(10, duracion);
                        ps.executeUpdate();
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) idTourExistente = rs.getInt(1);
                        }
                    }
                }

                if (idTourExistente > 0) {
                    boolean tieneImg = false;
                    try (PreparedStatement ps = conn.prepareStatement("SELECT idImagen FROM TourImagen WHERE idTour = ?")) {
                        ps.setInt(1, idTourExistente);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) tieneImg = true;
                        }
                    }
                    if (!tieneImg) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO TourImagen (idTour, url, esPrincipal, orden) VALUES (?, ?, 1, 1)")) {
                            ps.setInt(1, idTourExistente);
                            ps.setString(2, img);
                            ps.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();
            System.out.println("=== 3. DATOS SEMBRADOS Y SINCRONIZADOS EXITOSAMENTE PARA LAS 6 AGENCIAS ===");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
