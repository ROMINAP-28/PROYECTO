-- Travelink: crea una fecha futura de demostración para cada tour ACTIVO que no tenga fecha ese día.
-- Ejecutar una sola vez en DBeaver, conectado a DBTravelink.
-- No borra ni modifica reservas existentes.
INSERT INTO TourFecha (idTour, fecha, horaInicio, cupoTotal, cupoDisponible, estado)
SELECT t.idTour, DATE_ADD(CURDATE(), INTERVAL 14 DAY), '09:00:00', 30, 30, 'DISPONIBLE'
FROM Tour t
WHERE UPPER(COALESCE(t.estado, '')) = 'ACTIVO'
  AND NOT EXISTS (
      SELECT 1 FROM TourFecha tf
      WHERE tf.idTour = t.idTour
        AND tf.fecha = DATE_ADD(CURDATE(), INTERVAL 14 DAY)
  );

-- Comprobación de solo lectura:
SELECT tf.idTourFecha, tf.idTour, t.nombre AS nombre_tour, tf.fecha,
       tf.horaInicio, tf.cupoDisponible, tf.estado
FROM TourFecha tf
JOIN Tour t ON t.idTour = tf.idTour
WHERE tf.fecha >= CURDATE()
ORDER BY tf.fecha, t.nombre;
