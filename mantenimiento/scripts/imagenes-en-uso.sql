-- ==============================================================================
-- TRAVELINK - Consulta de Rutas de Imágenes Almacenadas en Base de Datos
-- ==============================================================================
-- Ejecutar en DBeaver, MySQL Workbench o CLI para auditar qué imágenes están
-- activamente referenciadas en las tablas del sistema.

-- 1. Imágenes de Galería y Portada de Tours (Tabla TourImagen)
SELECT 
    'TourImagen' AS origen_tabla,
    ti.idTour,
    t.nombre AS nombre_tour,
    ti.url AS ruta_imagen,
    ti.esPrincipal,
    ti.orden
FROM TourImagen ti
LEFT JOIN Tour t ON ti.idTour = t.idTour
ORDER BY ti.idTour, ti.orden;

-- 2. Logotipos y Banners de Agencias Turísticas (Tabla Agencia)
SELECT 
    'Agencia' AS origen_tabla,
    idAgencia,
    nombreComercial,
    logoUrl AS ruta_imagen,
    NULL AS esPrincipal,
    NULL AS orden
FROM Agencia
WHERE logoUrl IS NOT NULL AND logoUrl != '';

-- 3. Comprobantes de Pago Subidos por Turistas (Tabla Pago)
SELECT 
    'Pago' AS origen_tabla,
    idPago,
    codigoOperacion,
    COALESCE(urlComprobante, url) AS ruta_imagen,
    NULL AS esPrincipal,
    NULL AS orden
FROM Pago
WHERE COALESCE(urlComprobante, url) IS NOT NULL;

-- 4. Consolidado Unificado de Todas las Rutas de Imágenes Registradas
SELECT DISTINCT ruta_imagen, origen_tabla FROM (
    SELECT url AS ruta_imagen, 'TourImagen' AS origen_tabla FROM TourImagen WHERE url IS NOT NULL
    UNION
    SELECT logoUrl AS ruta_imagen, 'Agencia' AS origen_tabla FROM Agencia WHERE logoUrl IS NOT NULL
    UNION
    SELECT COALESCE(urlComprobante, url) AS ruta_imagen, 'Pago' AS origen_tabla FROM Pago WHERE COALESCE(urlComprobante, url) IS NOT NULL
) AS imagenes_totales
ORDER BY origen_tabla, ruta_imagen;
