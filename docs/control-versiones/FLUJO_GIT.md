# Flujo de Trabajo en Git y Gestión de Versiones

## 1. Modelo de Ramificación

El proyecto TRAVELINK adopta una variante ágil y simplificada de **GitFlow / GitHub Flow** orientada a integración y despliegue continuo:

```text
  main (producción estable, tags vX.Y.Z)
   │
   ├── develop / release
   │    │
   │    ├── feature/catalogo-tours
   │    ├── fix/validacion-ruc-agencia
   │    └── chore/cerrar-brechas-seguridad
```

### Ramas Principales
- **`main`**: Código en estado productivo. Cada merge a `main` debe corresponder a un incremento estable respaldado por pruebas automatizadas aprobadas. Se etiqueta con releases semánticos (`v1.0.0`, `v1.1.0`).
- **`chore/*` / `feature/*` / `fix/*`**: Ramas de trabajo temporales creadas para implementar capacidades, subsanar defectos o reorganizar infraestructura. Se integran mediante Pull Requests auditados.

---

## 2. Convención de Mensajes de Confirmación (Conventional Commits)

Los commits del repositorio siguen el estándar de **Conventional Commits 1.0**:

```text
<tipo>(<alcance opcional>): <descripción corta en imperativo>

[cuerpo explicativo opcional]

[referencias a issues o tickets opcionales]
```

### Tipos Permitidos
- `feat`: Nueva funcionalidad o capacidad para el usuario final o plataforma.
- `fix`: Corrección de un defecto o vulnerabilidad de software.
- `docs`: Modificaciones exclusivas en documentación técnica o diagramas (`docs/`).
- `test`: Incorporación o refactorización de pruebas unitarias, de integración o carga.
- `chore`: Tareas de mantenimiento, dependencias, scripts o reorganización estructural sin alterar lógica de negocio.
- `refactor`: Cambios de código que ni añaden características ni corrigen errores.
- `ci`: Modificaciones en flujos de automatización (`.github/workflows/`).

---

## 3. Políticas de Calidad y Pull Requests

1. **Compilación Limpia Obligatoria**: Todo PR debe compilar exitosamente sin fallos de compilación con `./mvnw -B clean compile`.
2. **Suite de Pruebas en Verde**: Las 20 pruebas unitarias de `Springboot/test/` deben ejecutarse y pasar al 100% (`./mvnw -B test`).
3. **Escaneo de Vulnerabilidades (Trivy)**: El flujo de GitHub Actions (`ci.yml`) verifica que no existan vulnerabilidades críticas no mitigadas en el árbol de dependencias (`.trivyignore` auditado).
4. **Verificación ZAP**: En entornos integrados, la ejecución del escaneo DAST de OWASP ZAP no debe generar hallazgos de severidad High nuevos.
5. **Revisión de Pares**: Cada cambio sustancial en la lógica o infraestructura requiere aprobación y verificación de no-regresión.
