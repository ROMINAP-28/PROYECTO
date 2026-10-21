# TRAVELINK 🌍✈️
> Plataforma Integral de Turismo, Gestión de Paquetes, Reservas y Liquidación de Comisiones.

TRAVELINK es una aplicación web empresarial orientada al sector turístico del Perú que conecta a tres actores fundamentales:
1. **Turistas:** Exploración de destinos y tours, reserva en línea, cálculo automático de tarifas (adultos, niños, bebés), comprobantes en PDF y calificaciones con reseñas.
2. **Agencias de Viaje:** Registro formal con validación de RUC, publicación de paquetes turísticos, programación de salidas (fechas, cupos, horarios), carga masiva de disponibilidad vía Excel/CSV y reportes operacionales.
3. **Administradores:** Aprobación de agencias, monitoreo de métricas en dashboard, liquidación de comisiones de plataforma (15%), moderación de calidad y auditoría de usuarios.

---

## 🛠️ Stack Tecnológico

| Capa | Tecnologías |
|---|---|
| **Lenguaje y Runtime** | Java 21 (LTS) |
| **Framework Backend** | Spring Boot 3.3.4 |
| **Seguridad** | Spring Security 6 (BCrypt, CSRF con cookies, control por roles, bloqueo tras 5 intentos fallidos, cabeceras seguras) |
| **Persistencia Principal (DAO)** | Spring Data JPA / Hibernate (CRUD y consultas por entidad) |
| **Persistencia Especializada** | JDBC Nativo con `PreparedStatement` (Agregaciones de dashboard, JOINs complejos y reportes) |
| **Motor de Base de Datos** | MySQL 8.x (Instancia en la nube Aiven / Local) |
| **Generación de Reportes** | OpenPDF 1.3.39 (PDF) y Apache POI 5.2.5 (Excel .xlsx) |
| **Carga Masiva** | Apache POI (Excel) y Parser RFC 4180 (CSV) |
| **Logging** | SLF4J con Logback (rotación diaria, compresión gz y retención 30 días) |
| **Frontend** | Vanilla HTML5, CSS3 y JavaScript moderno (arquitectura desacoplada, sin dependencias pesadas) |
| **Pruebas** | JUnit 5 y Mockito (`spring-boot-starter-test`) |

---

## 📋 Requisitos Previos

- **Java Development Kit (JDK):** Versión 21 o superior configurada en `JAVA_HOME`.
- **Git:** Versión 2.30+ para clonado y flujo de ramas.
- **Base de Datos:** Instancia accesible de MySQL 8.0+.
- **Herramientas de Cliente MySQL (Opcional):** `mysql` y `mysqldump` para scripts de backup/restore.

---

## ⚙️ Configuración de Variables de Entorno

Por motivos de seguridad, las credenciales de la base de datos **NUNCA** se almacenan en código fuente ni en el repositorio.

1. Copie la plantilla `.env.example` creando el archivo `.env` en la raíz del proyecto:
   ```powershell
   copy .env.example .env
   ```
2. Configure sus credenciales reales en `.env`:
   ```properties
   # URL JDBC hacia su base de datos MySQL
   DB_URL=jdbc:mysql://localhost:3306/DBTravelink?ssl-mode=REQUIRED&useSSL=false&serverTimezone=UTC

   # Credenciales de acceso
   DB_USERNAME=root
   DB_PASSWORD=su_contraseña_segura
   ```
3. Si alguna de estas tres variables no está definida, la aplicación abortará el inicio con un mensaje claro en los registros protegiendo el entorno.

---

## 🚀 Cómo Ejecutar la Aplicación

### Opción 1: Script Automático (Recomendado en Windows)
Haga doble clic o ejecute desde la terminal:
```cmd
ejecutar.bat
```
El script cargará automáticamente las variables de `.env` e iniciará Spring Boot en el puerto `8080`.

### Opción 2: Usando el Maven Wrapper oficial
En Windows (PowerShell / CMD):
```powershell
.\mvnw.cmd spring-boot:run
```
En Linux / macOS:
```bash
./mvnw spring-boot:run
```

La aplicación estará disponible inmediatamente en:
👉 **http://localhost:8080**

### Ejecutar Pruebas Automatizadas
```powershell
.\mvnw.cmd test
```

---

## 📁 Estructura del Proyecto

```text
PROYECTO/
├── .env.example                 # Plantilla pública de variables de entorno
├── .gitignore                   # Exclusiones de Git (.env, IDEs, logs, binarios)
├── README.md                    # Documentación general del proyecto
├── pom.xml                      # Configuración Maven y dependencias oficiales
├── ejecutar.bat                 # Script de arranque en Windows con carga de .env
├── mvnw / mvnw.cmd              # Maven Wrapper oficial
├── docs/                        # Documentación técnica y arquitectura
│   └── ARQUITECTURA.md          # Especificación detallada de capas y diseño
├── sql/                         # Scripts de base de datos
│   ├── travelink_schema.sql     # Schema DDL estándar (17 tablas)
│   ├── datos_minimos.sql        # Semilla con roles, admin y 25 departamentos
│   ├── backup.bat / backup.sh   # Scripts de respaldo seguro
│   ├── restore.bat / restore.sh # Scripts de restauración segura
│   └── backups/                 # Almacén de respaldos (.sql)
└── Springboot/
    ├── resources/
    │   ├── application.properties # Configuración Spring Boot
    │   ├── logback.xml            # Configuración de rotación de logs
    │   └── static/                # Frontend desacoplado
    │       ├── css/               # Hojas de estilo por módulo
    │       ├── js/                # Controladores JavaScript por módulo
    │       └── html/              # Vistas HTML (turista, agencia, administrador)
    ├── src/com/travelink/
    │   ├── TravelinkApplication.java # Clase principal Spring Boot
    │   ├── config/                # Seguridad, CORS, CSRF, Conexión y Manejador Global
    │   ├── controlador/           # Controladores Spring MVC REST (@RestController)
    │   │   └── dto/               # Objetos de Transferencia de Datos (DTOs de entrada y salida)
    │   ├── servicio/              # Interfaces de negocio (SOLID)
    │   │   └── impl/              # Implementaciones con inyección por constructor
    │   ├── repositorio/           # DAO Principal: Spring Data JPA Repositories
    │   ├── persistencia/          # DAO Especializado: Consultas complejas JDBC
    │   ├── entidades/             # Entidades de dominio JPA
    │   └── reportes/              # Generadores de PDF (OpenPDF) y Excel (Apache POI)
    └── test/com/travelink/        # Pruebas unitarias con JUnit 5 y Mockito
```

---

## 🌿 Flujo de Ramas Git (Git Flow)

Para el desarrollo colaborativo y los talleres de control de versiones, se adopta el modelo **Git Flow estándar**:

```mermaid
gitGraph
   commit id: "v1.0.0 (Init)"
   branch develop
   checkout develop
   commit id: "Setup develop"
   branch feature/reserva-cupos
   checkout feature/reserva-cupos
   commit id: "feat: logica atómica de cupos"
   commit id: "test: pruebas unitarias"
   checkout develop
   merge feature/reserva-cupos
   branch feature/importacion-excel
   checkout feature/importacion-excel
   commit id: "feat: servicio Apache POI"
   checkout develop
   merge feature/importacion-excel
   checkout master
   merge develop tag: "v1.1.0"
   branch hotfix/seguridad-credenciales
   checkout hotfix/seguridad-credenciales
   commit id: "fix: lectura obligatoria de .env"
   checkout master
   merge hotfix/seguridad-credenciales tag: "v1.1.1"
   checkout develop
   merge hotfix/seguridad-credenciales
```

### Reglas de Ramas:
1. **`master` / `main`:**
   - Rama protegida de **producción**.
   - Solo recibe merges desde `develop` (al consolidar un release) o desde ramas `hotfix/*`.
   - Cada commit en `master` lleva una etiqueta de versión semántica (ej. `v1.0.0`).
2. **`develop`:**
   - Rama principal de **integración activa**.
   - Contiene el código probado y listo para el próximo ciclo de despliegue.
3. **`feature/<nombre-funcionalidad>`:**
   - Nacen desde: `develop`.
   - Se integran en: `develop` mediante Pull Request / Merge Request tras superar pruebas (`mvn test`).
   - Ejemplos: `feature/importar-disponibilidad`, `feature/reportes-estandarizados`.
4. **`hotfix/<nombre-incidencia>`:**
   - Nacen desde: `master` para parches críticos en producción.
   - Se integran en: **ambas** ramas (`master` y `develop`) para evitar regresiones.
