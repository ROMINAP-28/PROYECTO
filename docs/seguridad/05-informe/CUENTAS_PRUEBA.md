# Cuentas Sintéticas de Prueba - Entorno Local TRAVELINK

> [!IMPORTANT]
> **ENTORNO LOCAL EXCLUSIVAMENTE.**
> Todas las cuentas, correos, documentos, tarjetas y teléfonos listados en este documento son **100% ficticios**, creados exclusivamente para la ejecución de pruebas de seguridad académicas en el laboratorio local. No corresponden a usuarios, agencias ni personas reales.

---

## 1. Credenciales de Acceso Sintéticas

| Rol | Correo / Usuario | Contraseña Plana | Hash Almacenado (BCrypt) | ID Usuario | ID Entidad |
|---|---|---|---|---|---|
| **Turista A** | `turista_a@test.local` | `TestTurista#2026` | `$2a$10$CrJmkehOIC2FYa/BKn9DHOAfa66sHxCTpp/BUCNS4BvD9bg7nMdGS` | 100 | Persona: 100 |
| **Turista B** | `turista_b@test.local` | `TestTurista#2027` | `$2a$10$Dt8Z8dXSlhqTRG7l8sa/ZONMS1gVhuUII8bmuc9CUbaCNSmgtrTa.` | 101 | Persona: 101 |
| **Agencia** | `agencia_demo@test.local` | `TestAgencia#2026` | `$2a$10$mrKLcGFE3gRgcbfpxkLW5OKAnjtIgeUn1g0lz8nndDs0tudhC3HAy` | 102 | Agencia: 10 |
| **Administrador** | `admin_seguro@test.local` | `TestAdmin#2026` | `$2a$10$XeHmUgMXoQseIKl0nXAX8etkY8zwe7oshdWgcrJEAeVhSdDPI9AZO` | 103 | Persona: 103 |

> [!NOTE]
> Ninguna de estas contraseñas coincide con los valores del bypass de desarrollo (`123456`, `12345678`, `pass123`, `admin`, `admin123`) presentes en `UsuarioServicioImpl.java`. Los hashes fueron generados por la clase `BCryptPasswordEncoder` de Spring Security.

---

## 2. Datos de Prueba Asociados (Base para Pruebas IDOR y Control de Acceso)

### 2.1 Turista A (`turista_a@test.local`)
- **DNI:** `10000001`
- **Reserva asignada:** `#500`
- **Código de reserva:** `TRV-A100-TEST`
- **Tour:** `Tour Sintético Machu Picchu Mágico` (ID 100)
- **Monto total:** S/ 500.00
- **Tarjeta ficticia:** VISA terminada en `1111`
- **Pasajeros:** 2 adultos (Turista Alfa y Acompañante Alfa)

### 2.2 Turista B (`turista_b@test.local`)
- **DNI:** `10000002`
- **Reserva asignada:** `#501`
- **Código de reserva:** `TRV-B101-TEST`
- **Tour:** `City Tour Lima Colonial Sintético` (ID 101)
- **Monto total:** S/ 160.00
- **Tarjeta ficticia:** MASTERCARD terminada en `2222`
- **Pasajeros:** 2 adultos (Turista Beta y Acompañante Beta)

### 2.3 Agencia Demo (`agencia_demo@test.local`)
- **RUC:** `20999999991`
- **Razón Social:** `Agencia Demo S.A.C.`
- **Nombre Comercial:** `Agencia Demo Tours`
- **Estado Solicitud:** `Aceptado` (requisito de `AuthApiController:163` para permitir inicio de sesión)

