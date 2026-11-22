# Plan Maestro de Mantenimiento y Continuidad Operativa - TRAVELINK

> **Documento Oficial de Ingeniería de Operaciones**  
> **Plataforma:** TRAVELINK - Sistema Web de Gestión Turística  
> **Versión:** 1.0.0  
> **Estado:** VALIDADO (Localmente en PC / Entorno Docker) / DOCUMENTADO (Producción Cloud Render & Aiven)  
> **Fecha:** Noviembre 2026  

---

## 1. Objetivos y Alcance del Mantenimiento

El presente plan establece los procedimientos sistemáticos de mantenimiento preventivo, predictivo y correctivo para la plataforma TRAVELINK, garantizando la alta disponibilidad, integridad de los datos, resiliencia ante desastres y ciclo de vida de dependencias y certificados.

### 1.1 Objetivos de Recuperación (RPO / RTO)

| Parámetro | Objetivo | Justificación Técnica | Implementación |
| :--- | :--- | :--- | :--- |
| **RPO** (*Recovery Point Objective*) | **<= 24 Horas** (Local) / **<= 5 Min** (Aiven) | Máxima pérdida de datos admisible en caso de catástrofe total. | Backups diarios automáticos a las 02:00 AM (`mysqldump --single-transaction`) + WAL PITR continuo en Aiven MySQL. |
| **RTO** (*Recovery Time Objective*) | **<= 30 Minutos** | Tiempo máximo para restaurar el servicio completo a estado operativo. | Restauración automatizada mediante [`mantenimiento/scripts/restore.ps1`](../../mantenimiento/scripts/restore.ps1) / `restore.sh` y validación con `restore-test.sh`. |

---

## 2. Matriz de Tareas de Mantenimiento

| Frecuencia | Tarea de Mantenimiento | Script / Automatización | Responsable | Estado |
| :--- | :--- | :--- | :--- | :--- |
| **Cada 5 min** | Sondeo de salud Actuator (`/actuator/health`) | [`mantenimiento/scripts/healthcheck.ps1`](../../mantenimiento/scripts/healthcheck.ps1) / `.sh` | Systemd Timer / Cron | **VALIDADO** |
| **Diario (02:00)** | Respaldo consistente MySQL con Gzip y SHA-256 | [`mantenimiento/scripts/backup.ps1`](../../mantenimiento/scripts/backup.ps1) / `.sh` | Task Scheduler / Cron | **VALIDADO** |
| **Semanal (Dom 02:30)** | Prueba automatizada de restauración en base temporal | [`mantenimiento/scripts/restore-test.ps1`](../../mantenimiento/scripts/restore-test.ps1) / `.sh` | Cron / Systemd | **VALIDADO** |
| **Semanal (Dom 03:00)** | Purga y rotación de logs históricos (> 14 días) | [`mantenimiento/scripts/limpiar-logs.ps1`](../../mantenimiento/scripts/limpiar-logs.ps1) / `.sh` | Logrotate / Cron | **VALIDADO** |
| **Semanal (Lun 04:00)** | Escaneo SCA Trivy y reporte de versiones Maven | [`.github/workflows/mantenimiento.yml`](../../.github/workflows/mantenimiento.yml) | GitHub Actions | **VALIDADO** |
| **Semanal (Lun 05:00)** | Pull Requests automáticos de parches de dependencias | [`.github/dependabot.yml`](../../.github/dependabot.yml) | Dependabot | **VALIDADO** |
| **Bimestral** | Renovación de certificados TLS/SSL Let's Encrypt | Certbot / Nginx Auto-Renew (`certbot renew`) | Ingeniero DevOps | **DOCUMENTADO** |
| **Continuo** | Backups gestionados y Point-In-Time-Recovery (PITR) | Consola Aiven Cloud MySQL | Proveedor Cloud | **DOCUMENTADO** |

---

## 3. Política de Respaldo y Restauración de Datos

### 3.1 Procedimiento de Respaldo (`backup.ps1` / `backup.sh`)
* **Consistencia Transaccional:** Se invoca `mysqldump` con el flag `--single-transaction`, permitiendo realizar volcados consistentes en caliente sobre tablas InnoDB sin bloquear lecturas ni escrituras de los usuarios.
* **Compresión Inmediata:** Los volcados `.sql` se comprimen nativamente en formato `.sql.gz`, logrando una reducción de espacio de almacenamiento superior al 80%.
* **Integridad Criptográfica:** Cada archivo `.sql.gz` genera de inmediato un archivo gemelo `.sha256` con el hash digital. Cualquier alteración de bytes es detectada previo a cualquier restauración.
* **Política de Retención (7 Diarios + 4 Semanales):**
  * Conserva los últimos 7 respaldos diarios.
  * Conserva 1 respaldo dominical por semana durante 4 semanas consecutivas.
  * Purga automáticamente respaldos con mayor antigüedad para optimizar el almacenamiento.

### 3.2 Prueba de Restauración (`restore-test.ps1` / `restore-test.sh`)
Para evitar el "respaldo de Schrödinger" (creer que se tienen copias sin saber si funcionan), el sistema ejecuta una prueba automatizada:
1. Verifica el checksum SHA-256 del último backup.
2. Descomprime el archivo en un entorno volátil temporal.
3. Restaura las tablas en una base aislada de pruebas (`travelink_restore_test`).
4. Audita y cuenta registros en tablas críticas (`Usuario`, `Tour`, `Reserva`, `Destino`, `Agencia`).
5. Destruye la base temporal y reporta el estado de integridad.

### 3.3 Capa Adicional: Backups Gestionados en Aiven MySQL (Cloud) `[DOCUMENTADO]`
En el entorno productivo de nube, Aiven Cloud proporciona una capa de defensa en profundidad complementaria:
* **Backups Automáticos Diarios:** Instantáneas completas de almacenamiento en frío retenidas por la plataforma de Aiven.
* **Point-in-Time Recovery (PITR):** Aiven replica continuamente los *write-ahead logs* (binlogs), permitiendo restaurar la base de datos a cualquier segundo específico dentro de la ventana de retención.
* **Aislamiento de Producción:** Las tareas de prueba y mantenimiento locales **nunca** se ejecutan contra la base de datos de producción en Aiven.

---

## 4. Mantenimiento de Dependencias y Certificados

### 4.1 Ciclo de Vida de Dependencias (SCA & Dependabot)
* **Dependabot (`.github/dependabot.yml`):**
  * **Maven:** Escanea `pom.xml` semanalmente (lunes a las 05:00 Lima) y genera Pull Requests para parches de seguridad y versiones menores.
  * **Docker:** Supervisa imágenes base (`eclipse-temurin:17-jre-jammy`, `nginx:alpine`) semanalmente.
  * **GitHub Actions:** Supervisa versiones de acciones (`actions/checkout`, `setup-java`, etc.) mensualmente.
* **Auditoría Semanal de Seguridad (`.github/workflows/mantenimiento.yml`):**
  * Ejecuta `versions:display-dependency-updates` para identificar dependencias obsoletas.
  * Ejecuta escaneo estático de vulnerabilidades con Aqua Trivy 0.28.0 aplicando `.trivyignore`.
  * Publica los reportes generados como artefactos versionados en GitHub Actions.

### 4.2 Mantenimiento de Certificados TLS / Let's Encrypt `[DOCUMENTADO]`
* En producción con Nginx (`despliegue/nginx/conf.d/travelink.conf`), los certificados SSL/TLS emitidos por Let's Encrypt tienen una validez de 90 días.
* Se programa la renovación automática vía Certbot cada 60 días:
  ```bash
  0 3 1 */2 * certbot renew --quiet --post-hook "nginx -s reload"
  ```
* En Render Cloud, la terminación TLS la gestiona automáticamente el balanceador de carga del proveedor con renovación gestionada sin intervención manual.

---

## 5. Orquestación y Programación de Tareas

El repositorio provee la configuración lista para desplegarse según el sistema operativo del host:

1. **Linux (Cron):** [`mantenimiento/cron/crontab.txt`](../../mantenimiento/cron/crontab.txt)
2. **Linux (Systemd Units & Timers):**
   * [`travelink-backup.service`](../../mantenimiento/systemd/travelink-backup.service) y [`travelink-backup.timer`](../../mantenimiento/systemd/travelink-backup.timer)
   * [`travelink-cleanup.service`](../../mantenimiento/systemd/travelink-cleanup.service) y [`travelink-cleanup.timer`](../../mantenimiento/systemd/travelink-cleanup.timer)
   * [`travelink-healthcheck.service`](../../mantenimiento/systemd/travelink-healthcheck.service) y [`travelink-healthcheck.timer`](../../mantenimiento/systemd/travelink-healthcheck.timer)
3. **Linux (Logrotate):** [`mantenimiento/logrotate/travelink`](../../mantenimiento/logrotate/travelink)
4. **Windows (Task Scheduler):** [`mantenimiento/windows/registrar-tarea.ps1`](../../mantenimiento/windows/registrar-tarea.ps1) (Registra la tarea `TRAVELINK_Backup_Diario` a las 02:00 AM).

---

## 6. Evidencias de Ejecución y Validación

Todas las evidencias generadas durante la ejecución real en entorno local se encuentran almacenadas en el directorio [`mantenimiento/evidencias/`](../../mantenimiento/evidencias/):

| Evidencia | Archivo | Descripción del Resultado |
| :--- | :--- | :--- |
| **Respaldo Real** | [`backup-real.txt`](../../mantenimiento/evidencias/backup-real.txt) | Volcado `.sql.gz` de 4.27 KB generado con éxito y retención aplicada. |
| **Checksum SHA-256** | [`checksum.txt`](../../mantenimiento/evidencias/checksum.txt) | Hash digital SHA-256 verificado (`8468fa2eaf56...`). |
| **Prueba de Restauración** | [`restore-test.txt`](../../mantenimiento/evidencias/restore-test.txt) | Descompresión, integridad de sintaxis DDL/DML y conteo de tablas maestras. |
| **Rotación de Retención** | [`rotacion-retencion.txt`](../../mantenimiento/evidencias/rotacion-retencion.txt) | Simulación de retención 7 diarios + 4 semanales con purga de backups caducados. |
| **Tarea Windows** | [`tarea-programada-windows.txt`](../../mantenimiento/evidencias/tarea-programada-windows.txt) | Tarea `TRAVELINK_Backup_Diario` registrada y consultada en Task Scheduler. |
| **Workflow GitHub** | [`workflow-mantenimiento.txt`](../../mantenimiento/evidencias/workflow-mantenimiento.txt) | Auditoría de dependencias y validación sintáctica del workflow semanal. |

---

## 7. Activos y Candidatas Pendientes de Confirmación

En concordancia con las políticas de limpieza segura y preservación de consistencia arquitectónica:

### 7.1 Imágenes Candidatas a Depuración (Pendientes de Confirmación)
Las siguientes 15 imágenes estáticas han sido identificadas como potenciales candidatas a limpieza. **NO se han borrado** a la espera de que el administrador ejecute el script [`mantenimiento/scripts/imagenes-en-uso.sql`](../../mantenimiento/scripts/imagenes-en-uso.sql) en su cliente de base de datos (DBeaver / MySQL Workbench) para confirmar si están enlazadas en registros activos de producción:

1. `img/pisco.jpg`
2. `img/fondologinadmin.png`
3. `img/machupicchu.png`
4. `img/tours/pasco_punrun.jpg`
5. `img/tours/lima_barranco.jpg`
6. `img/tours/madrededios_collpa.jpg`
7. `img/tours/moquegua_torata.jpg`
8. `img/tours/tacna_termales.jpg`
9. `img/tours/pasco_huayllay.jpg`
10. `img/tours/tacna_valle_viejo.jpg`
11. `img/tours/moquegua_ruta_vino.jpg`
12. `img/tours/ilo_playas.jpg`
13. `img/tours/madrededios_sandoval.jpg`
14. `img/tours/lima_colonial.jpg`
15. `img/tours/ilo_punta_coles.jpg`

### 7.2 Clases Java: Citas en PATRONES.md vs Depuración
* **Clases Conservadas por Valor Arquitectónico y Cita Formal en [`PATRONES.md`](../ingenieria/diseno/PATRONES.md):**
  * DTOs representativos de transferencia: `ReservaDto`, `TourDto`, `PasajeroDto`, `CalificacionDto`, `ComisionDto`, `ImportacionResultadoDto`.
  * Repositorio JPA desacoplado: `ReservaRepositorio` (citado explícitamente como ejemplo de segregación de interfaces Spring Data JPA en la sección 2.1 y 2.3 de `PATRONES.md`).
* **Clases Eliminadas Previamente (Commit `b17287b`):**
  * Se eliminaron 7 clases sin referencias en controladores, sin serialización Jackson activa ni mención pedagógica en la documentación de patrones: `AgenciaDto`, `DestinoDto`, `UsuarioDto`, `AgenciaPersistencia`, `MetodoPagoPersistencia`, `PasajeroPersistencia`, `PersonaPersistencia`.

### 7.3 Archivos Estáticos Sin Uso Eliminados
Tras verificar que ningún archivo HTML de las 34 vistas los incluía mediante etiquetas `<script>` o `<link>`, se procedió a la remoción segura con `git rm` de:
* `Springboot/resources/static/js/turista/main.js`
* `Springboot/resources/static/js/administrador/admin.js`
* `Springboot/resources/static/css/administrador/admin.css`

