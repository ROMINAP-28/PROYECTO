# Consideraciones de Seguridad en Java y Spring Boot — TRAVELINK

Este documento sintetiza la postura de seguridad técnica implementada en la arquitectura de software de TRAVELINK, analizando la configuración central de Spring Security (`com.travelink.config.SecurityConfig`), la validación de entradas, la gestión de sesiones y secretos, y su correlación directa con los hallazgos de vulnerabilidad [H-01 a H-09](../03-hallazgos/) y el catálogo de dependencias [tabla-dependencias-cve.md](tabla-dependencias-cve.md).

---

## 1. Configuración Central de Seguridad (`SecurityConfig.java`)

La clase `com.travelink.config.SecurityConfig` define la cadena de filtros perimetrales (`SecurityFilterChain`) que rige el acceso a las APIs REST y recursos estáticos:

### 1.1 Autenticación y Hash de Contraseñas (BCrypt)
- **Proveedor**: `DaoAuthenticationProvider` acoplado con `CustomUserDetailsService`.
- **Cifrado Criptográfico**: `BCryptPasswordEncoder` configurado como bean singleton. Aplica sal aleatoria automática por cada credencial y factor de trabajo adaptativo (cost = 10 por defecto), protegiendo las contraseñas ante ataques de diccionario, fuerza bruta y tablas arcoíris (*rainbow tables*).
- **Mapeo a Hallazgos**: Previene la exposición de contraseñas en texto claro descrita en las buenas prácticas de [H-02](../03-hallazgos/H-02-Exposicion-Credenciales.md).

### 1.2 Control de Acceso Basado en Roles (RBAC)
Las directivas de autorización se estructuran en capas jerárquicas:
1. **Rutas Públicas (`permitAll`)**: Catálogo público (`/api/destinos/**`, `/api/tours/**`), endpoints de registro y login (`/api/auth/**`), y recursos estáticos (`/css/**`, `/js/**`, `/img/**`).
2. **Rutas de Turista (`authenticated`)**: Panel personal (`/html/turista/reservas.html`) y operaciones transaccionales (`/api/turista/**`).
3. **Rutas de Agencia (`hasRole('AGENCIA')`)**: Módulo de tours propios, calendario de fechas y balance (`/api/agencia/**`, `/html/agencia/**`).
4. **Rutas de Administrador (`hasRole('ADMIN')`)**: Gestión global de usuarios, aprobación de agencias y corte de comisiones (`/api/admin/**`, `/html/administrador/**`).
5. **Comprobantes y Vouchers**: `/api/turista/reservas/*/comprobante.pdf` permite acceso a roles `TURISTA`, `AGENCIA` y `ADMIN`, pero el controlador `ReportesController` ejecuta una segunda validación programática a nivel de fila (`authUserId == resIdUsuario || isAgenciaDueña || isAdmin`) para mitigar vulnerabilidades IDOR.
- **Mapeo a Hallazgos**: Directamente relacionado con [H-03 (IDOR en Reservas)](../03-hallazgos/H-03-IDOR-Reservas.md) y [H-04 (Control de Acceso Administrativo)](../03-hallazgos/H-04-Acceso-Admin.md).

### 1.3 Protección CSRF (Cross-Site Request Forgery)
- **Estrategia**: `CookieCsrfTokenRepository.withHttpOnlyFalse()` emite la cookie `XSRF-TOKEN` accesible por JavaScript cliente (`js/comun/api.js`), la cual debe ser devuelta en la cabecera HTTP `X-XSRF-TOKEN`.
- **Exclusiones Declaradas**: Mediante `ignoringRequestMatchers()`, se omiten rutas de consumo público o que operan con validación transaccional propia (ej. logins, catálogo de destinos, reservas).
- **Mapeo a Hallazgos**: Relacionado con [H-05 (Riesgo de Exclusión CSRF)](../03-hallazgos/H-05-CSRF.md).

### 1.4 Política de Compartición de Recursos entre Orígenes (CORS)
- **Implementación**: `corsConfigurationSource()` restringe los métodos permitidos (`GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`) y valida los orígenes autorizados evitando comodines universales (`*`) en endpoints con intercambio de credenciales.
- **Mapeo a Hallazgos**: Relacionado con [H-06 (Configuración CORS)](../03-hallazgos/H-06-CORS.md).

### 1.5 Cabeceras de Seguridad Perimetral
- `frameOptions.sameOrigin()`: Mitiga ataques de Clickjacking impidiendo que TRAVELINK sea embebido en `<iframe>` de dominios externos.
- `contentTypeOptions()`: Activa `X-Content-Type-Options: nosniff`.
- `xssProtection()`: Habilita el filtro de XSS del navegador (`X-XSS-Protection`).
- **Mapeo a Hallazgos**: Relacionado con [H-08 (Cabeceras de Seguridad Faltantes)](../03-hallazgos/H-08-Cabeceras-Seguridad.md).

---

## 2. Validación de Entradas y Prevención de Inyecciones

### 2.1 Desacoplamiento con DTOs y Validación de Esquema
Todas las solicitudes que modifican estado consumen DTOs dedicados (`RegistroTuristaRequest`, `ReservaRequest`, `TourRequest`) decorados con validadores Jakarta Bean Validation:
- `@NotBlank`, `@Size(max = 150)` para campos de texto.
- `@Email` para normalización sintáctica de correos.
- `@Positive`, `@Min(1)` para tarifas, cupos y pasajeros.

### 2.2 Prevención de Inyección SQL (SQLi)
- **Spring Data JPA**: Las consultas generadas por el ORM utilizan binding de variables automático provisto por Hibernate.
- **Capa DAO Nativa (`com.travelink.persistencia`)**: Todas las consultas complejas de `ReservaDAO`, `TourDAO`, `AgenciaDAO` y `ReportesController` utilizan estrictamente `java.sql.PreparedStatement` con parámetros posicionales (`?`), descartando la concatenación de cadenas sin escapar.
- **Mapeo a Hallazgos**: Mitigación verificada en [H-01 (Inyección SQL)](../03-hallazgos/H-01-SQL-Injection.md) y respaldada por los resultados limpios de SQLMap (`pruebas/evidencias/sqlmap/sqlmap-results.txt`).

---

## 3. Manejo de Sesiones y Protección de Secretos

### 3.1 Ciclo de Vida de Sesiones HTTP
- **Fijación de Sesión**: `sessionFixation().migrateSession()` regenera el ID de sesión tras el inicio de sesión exitoso, invalidando cualquier identificador previo que pudiera haber sido interceptado por un tercero.
- **Atributos de Cookie `JSESSIONID`**:
  - `http-only=true`: Impide el acceso a la cookie de sesión mediante `document.cookie` en JavaScript, neutralizando el robo de identidad por XSS reflejado o almacenado.
  - `same-site=lax`: Bloquea el envío automático de la cookie en peticiones entre sitios de terceros.
  - `timeout=120m`: Expiración automática tras 2 horas de inactividad.
- **Mapeo a Hallazgos**: Relacionado con [H-07 (Gestión de Sesiones y Cookies)](../03-hallazgos/H-07-Cookies-Sesion.md).

### 3.2 Desacoplamiento Estricto de Secretos
- En cumplimiento de los principios de factoría de software (Twelve-Factor App), **ninguna contraseña, cadena de conexión o credencial administrativa reside en el repositorio**.
- Las propiedades `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}` se inyectan en tiempo de ejecución desde variables de entorno provistas por el archivo `.env` (excluido en `.gitignore`) o por los secretos de GitHub Actions en los pipelines de CI/CD.

---

## 4. Matriz de Trazabilidad: Consideraciones vs Hallazgos y CVEs

| Componente Técnico en Código | Mecanismo de Seguridad | Hallazgo Relacionado | Estado de Seguridad |
|---|---|---|:---:|
| `ConexionDB` / DAOs | Consultas parametrizadas con `PreparedStatement` | [H-01 (SQLi)](../03-hallazgos/H-01-SQL-Injection.md) | Mitigado / Verificado con SQLMap |
| `UsuarioServicio` | Hashing con `BCryptPasswordEncoder` (cost 10) | [H-02 (Credenciales)](../03-hallazgos/H-02-Exposicion-Credenciales.md) | Mitigado / Verificado en DB |
| `ReportesController` | Validación de propiedad multitenant (`authUserId == resIdUsuario`) | [H-03 (IDOR)](../03-hallazgos/H-03-IDOR-Reservas.md) | Mitigado en código |
| `SecurityConfig` (RBAC) | `hasRole('ADMIN')` y `hasRole('AGENCIA')` por prefijo de ruta | [H-04 (Broken Access Control)](../03-hallazgos/H-04-Acceso-Admin.md) | Mitigado por Spring Security |
| `SecurityConfig` (CSRF) | `CookieCsrfTokenRepository` con lista de exclusión acotada | [H-05 (CSRF)](../03-hallazgos/H-05-CSRF.md) | Riesgo documentado / Triaje ZAP |
| `SecurityConfig` (CORS) | `CorsConfigurationSource` con orígenes explícitos | [H-06 (CORS)](../03-hallazgos/H-06-CORS.md) | Mitigado en configuración |
| `application.properties` | `JSESSIONID` con `HttpOnly=true`, `SameSite=Lax`, `migrateSession` | [H-07 (Cookies)](../03-hallazgos/H-07-Cookies-Sesion.md) | Mitigado en properties |
| `SecurityConfig` (Headers) | `frameOptions.sameOrigin()`, `contentTypeOptions()` | [H-08 (Cabeceras)](../03-hallazgos/H-08-Cabeceras-Seguridad.md) | HSTS/CSP pendiente en proxy Nginx |
| `pom.xml` (Spring Boot) | Actualización a Spring Boot 3.3.13 y `.trivyignore` auditado | [H-09 (SCA)](../03-hallazgos/H-09-Vulnerabilidades-Dependencias.md) | Mitigado / [tabla-dependencias-cve.md](tabla-dependencias-cve.md) |
