# Infraestructura y Procedimientos de Despliegue — TRAVELINK 🚀

Este directorio contiene los archivos ejecutables de infraestructura, configuraciones de servidor web, plantillas de orquestación, scripts de automatización de despliegue y sus respectivas evidencias técnicas.

---

## Estructura del Directorio `despliegue/`

```text
despliegue/
├── README.md                      # Este manual de infraestructura
├── docker/
│   └── docker-compose.prod.yml    # Orquestación de producción (DB + App + Nginx)
├── servidor/
│   ├── nginx.conf                 # Proxy inverso con cabeceras defensivas y compresión
│   └── travelink.service          # Archivo de unidad systemd para entornos VPS / Linux
├── scripts/
│   ├── deploy.sh / .ps1           # Automatización de despliegue, healthcheck y smoke test
│   └── rollback.sh / .ps1         # Procedimiento automatizado de reversión a versión estable
├── maven/
│   └── build.sh / .ps1            # Empaquetado de producción y cálculo de checksum SHA-256
└── evidencias/
    ├── antes/                     # Evidencias históricas de despliegue (ej. despliegue-local-3.3.13.txt)
    └── despues/                   # Evidencias posteriores a cambios de arquitectura y mitigaciones
```

---

## Modos de Despliegue Disponibles

1. **Despliegue Contenedorizado (Recomendado):**  
   Gestionado mediante Docker Compose (`despliegue/docker/docker-compose.prod.yml`) con Nginx como terminador perimetral de cabeceras de seguridad y proxy HTTP/1.1 hacia el contenedor Spring Boot.
2. **Despliegue Standalone (Systemd):**  
   Ejecución directa del JAR generado por Maven como servicio no privilegiado (`user: travelink`) bajo `systemd` (`despliegue/servidor/travelink.service`).
