package com.travelink.controlador;

import com.travelink.controlador.dto.AuthResponse;
import com.travelink.entidades.Agencia;
import com.travelink.entidades.SolicitudAgencia;
import com.travelink.entidades.Usuario;
import com.travelink.servicio.AgenciaServicio;
import com.travelink.servicio.LoginAttemptService;
import com.travelink.servicio.UsuarioServicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controlador REST para el subsistema de Autenticación y Sesión.
 * Aplica principios SOLID:
 * - SRP: Gestiona exclusivamente el ciclo de vida de autenticación, login, logout y perfil (/auth/me).
 * - DIP: Inyecta abstracciones de servicio (UsuarioServicio, AgenciaServicio) por constructor.
 */
@RestController
@RequestMapping("/api")
public class AuthApiController {

    private static final Logger log = LoggerFactory.getLogger(AuthApiController.class);

    private final UsuarioServicio usuarioServicio;
    private final AgenciaServicio agenciaServicio;
    private final LoginAttemptService loginAttemptService;
    private final PasswordEncoder passwordEncoder;

    public AuthApiController(UsuarioServicio usuarioServicio,
                             AgenciaServicio agenciaServicio,
                             LoginAttemptService loginAttemptService,
                             PasswordEncoder passwordEncoder) {
        this.usuarioServicio = usuarioServicio;
        this.agenciaServicio = agenciaServicio;
        this.loginAttemptService = loginAttemptService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping({"/api/login", "/auth/login", "/login", "/agencia/login", "/admin/login"})
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        String login = Optional.ofNullable(credentials.get("correo"))
                .or(() -> Optional.ofNullable(credentials.get("usuario")))
                .or(() -> Optional.ofNullable(credentials.get("usuarioCorreo")))
                .or(() -> Optional.ofNullable(credentials.get("email")))
                .or(() -> Optional.ofNullable(credentials.get("login")))
                .orElse("")
                .trim();

        String rawPassword = Optional.ofNullable(credentials.get("clave"))
                .or(() -> Optional.ofNullable(credentials.get("contrasena")))
                .or(() -> Optional.ofNullable(credentials.get("contraseña")))
                .or(() -> Optional.ofNullable(credentials.get("password")))
                .orElse("");

        if (login.isEmpty() || rawPassword.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", "Debe ingresar usuario/correo y contraseña"));
        }

        String clientIp = getClientIp(request);
        String lockoutKey = clientIp + ":" + login.toLowerCase();

        // Verificar si está bloqueado por 5 intentos fallidos
        if (loginAttemptService.isBlocked(lockoutKey)) {
            log.warn("Bloqueo de seguridad: intento de login rechazado para [{}] por superar 5 intentos fallidos desde IP: {}", login, clientIp);
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(Map.of("status", "error", "message", "Cuenta bloqueada temporalmente por 5 intentos fallidos. Intente nuevamente en 15 minutos."));
        }

        Optional<Usuario> usuarioOpt = usuarioServicio.buscarPorLogin(login);
        if (usuarioOpt.isEmpty()) {
            loginAttemptService.loginFailed(lockoutKey);
            int remaining = loginAttemptService.getRemainingAttempts(lockoutKey);
            log.warn("Intento de login fallido (usuario no encontrado): [{}] desde IP: {}. Intentos restantes: {}", login, clientIp, remaining);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Usuario o contraseña incorrectos. Intentos restantes: " + remaining));
        }

        Usuario usuario = usuarioOpt.get();

        // Validar contraseña
        boolean passwordOk = usuarioServicio.verificarContrasena(rawPassword, usuario.getContrasena());
        if (!passwordOk) {
            loginAttemptService.loginFailed(lockoutKey);
            int remaining = loginAttemptService.getRemainingAttempts(lockoutKey);
            log.warn("Intento de login fallido (contraseña errónea): [{}] desde IP: {}. Intentos restantes: {}", login, clientIp, remaining);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Usuario o contraseña incorrectos. Intentos restantes: " + remaining));
        }

        // Si la contraseña estaba en texto plano o formato antiguo, rehashear automáticamente con BCrypt
        if (usuario.getContrasena() == null || !usuario.getContrasena().startsWith("$2")) {
            usuario.setContrasena(passwordEncoder.encode(rawPassword));
            usuarioServicio.guardar(usuario);
        }

        // Validar si el usuario está activo
        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            log.warn("Intento de login con cuenta inactiva: [{}] desde IP: {}", login, clientIp);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", "error", "message", "La cuenta se encuentra inactiva. Contacte al soporte."));
        }

        // Resolver Rol
        String rolName = "ROLE_TURISTA";
        if (usuario.getIdRol() != null) {
            if (usuario.getIdRol() == 3) {
                rolName = "ROLE_ADMIN";
            } else if (usuario.getIdRol() == 2) {
                rolName = "ROLE_AGENCIA";
            }
        }

        // Para agencias: solo entran si su solicitud fue ACEPTADA por el admin
        Integer idAgencia = null;
        if ("ROLE_AGENCIA".equals(rolName)) {
            Optional<Agencia> agenciaOpt = agenciaServicio.buscarPorIdUsuario(usuario.getIdUsuario());
            List<SolicitudAgencia> solicitudes = agenciaServicio.listarSolicitudesPorUsuario(usuario.getIdUsuario());

            boolean tieneSolicitudAceptada = solicitudes.stream()
                    .anyMatch(s -> "Aceptado".equalsIgnoreCase(s.getEstado()) || "Aceptada".equalsIgnoreCase(s.getEstado()) || "ACTIVO".equalsIgnoreCase(s.getEstado()));

            boolean agenciaActiva = agenciaOpt.isPresent() && "ACTIVO".equalsIgnoreCase(agenciaOpt.get().getEstado());

            if (!tieneSolicitudAceptada && !agenciaActiva) {
                log.warn("Intento de login de agencia no aprobada: [{}] desde IP: {}", login, clientIp);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("status", "error", "message", "Su solicitud de agencia aún no ha sido aprobada por el administrador."));
            }

            if (agenciaOpt.isPresent()) {
                idAgencia = agenciaOpt.get().getIdAgencia();
            }
        }

        // Éxito: limpiar intentos fallidos
        loginAttemptService.loginSucceeded(lockoutKey);
        log.info("Login exitoso - Usuario: [{}] (ID: {}), Rol: [{}], IP: {}", usuario.getNombreUsuario(), usuario.getIdUsuario(), rolName, clientIp);

        // Configurar contexto de seguridad de Spring Security
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(rolName));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(usuario.getNombreUsuario(), null, authorities);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Guardar en sesión HTTP
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        session.setAttribute("ID_USUARIO", usuario.getIdUsuario());
        session.setAttribute("USUARIO_NOMBRE", usuario.getNombreUsuario());
        session.setAttribute("ROL", rolName);
        if (idAgencia != null) {
            session.setAttribute("ID_AGENCIA", idAgencia);
        }

        String nombreCompleto = usuario.getPersona() != null && usuario.getPersona().getNombre() != null && !usuario.getPersona().getNombre().isEmpty()
                ? usuario.getPersona().getNombreCompleto()
                : usuario.getNombreUsuario();

        String email = usuario.getPersona() != null ? usuario.getPersona().getEmail() : "";

        AuthResponse authResponse = new AuthResponse(
                usuario.getIdUsuario(),
                nombreCompleto,
                email,
                rolName,
                idAgencia
        );

        if (usuario.getPersona() != null) {
            authResponse.setNombres(usuario.getPersona().getNombre());
            String pat = usuario.getPersona().getApellidoPaterno();
            String mat = usuario.getPersona().getApellidoMaterno();
            authResponse.setApellidoPaterno(pat != null ? pat : "");
            authResponse.setApellidoMaterno(mat != null ? mat : "");
            String ape = ((pat != null ? pat : "") + " " + (mat != null ? mat : "")).trim();
            authResponse.setApellidos(ape);
            authResponse.setNroDocumento(usuario.getPersona().getNroDocumento());
            authResponse.setTelefono(usuario.getPersona().getTelefono());
        }

        if ("ROLE_AGENCIA".equals(rolName)) {
            Optional<Agencia> agOpt = agenciaServicio.buscarPorIdUsuario(usuario.getIdUsuario());
            if (agOpt.isPresent()) {
                Agencia ag = agOpt.get();
                authResponse.setNombreComercial(ag.getNombreComercial());
                authResponse.setRazonSocial(ag.getRazonSocial());
                if (ag.getNombreComercial() != null && !ag.getNombreComercial().isBlank()) {
                    authResponse.setNombre(ag.getNombreComercial());
                } else if (ag.getRazonSocial() != null && !ag.getRazonSocial().isBlank()) {
                    authResponse.setNombre(ag.getRazonSocial());
                }
                if ((authResponse.getCorreo() == null || authResponse.getCorreo().isBlank()) && ag.getEmail() != null) {
                    authResponse.setCorreo(ag.getEmail());
                }
                if (authResponse.getTelefono() == null && ag.getTelefono() != null) {
                    authResponse.setTelefono(ag.getTelefono());
                }
            }
        }

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping({"/auth/logout", "/logout"})
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        log.info("Cierre de sesión ejecutado para IP: {}", clientIp);
        return ResponseEntity.ok(Map.of("ok", true, "message", "Sesión cerrada correctamente"));
    }

    @GetMapping("/auth/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        HttpSession session = request.getSession(false);
        Integer idUsuario = session != null ? (Integer) session.getAttribute("ID_USUARIO") : null;
        Integer idAgencia = session != null ? (Integer) session.getAttribute("ID_AGENCIA") : null;

        Usuario usuario = null;
        if (idUsuario != null) {
            usuario = usuarioServicio.buscarPorId(idUsuario).orElse(null);
        }
        if (usuario == null) {
            String username = auth.getName();
            usuario = usuarioServicio.buscarPorCorreoOUsuario(username).orElse(null);
        }

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String rolName = "ROLE_TURISTA";
        if (usuario.getIdRol() != null) {
            if (usuario.getIdRol() == 3) rolName = "ROLE_ADMIN";
            else if (usuario.getIdRol() == 2) rolName = "ROLE_AGENCIA";
        }

        if ("ROLE_AGENCIA".equals(rolName) && idAgencia == null) {
            Optional<Agencia> ag = agenciaServicio.buscarPorIdUsuario(usuario.getIdUsuario());
            if (ag.isPresent()) {
                idAgencia = ag.get().getIdAgencia();
                if (session != null) session.setAttribute("ID_AGENCIA", idAgencia);
            }
        }

        String nombreCompleto = usuario.getPersona() != null && usuario.getPersona().getNombre() != null && !usuario.getPersona().getNombre().isEmpty()
                ? usuario.getPersona().getNombreCompleto()
                : usuario.getNombreUsuario();

        String email = usuario.getPersona() != null ? usuario.getPersona().getEmail() : "";

        AuthResponse authResponse = new AuthResponse(
                usuario.getIdUsuario(),
                nombreCompleto,
                email,
                rolName,
                idAgencia
        );

        if (usuario.getPersona() != null) {
            authResponse.setNombres(usuario.getPersona().getNombre());
            String pat = usuario.getPersona().getApellidoPaterno();
            String mat = usuario.getPersona().getApellidoMaterno();
            authResponse.setApellidoPaterno(pat != null ? pat : "");
            authResponse.setApellidoMaterno(mat != null ? mat : "");
            String ape = ((pat != null ? pat : "") + " " + (mat != null ? mat : "")).trim();
            authResponse.setApellidos(ape);
            authResponse.setNroDocumento(usuario.getPersona().getNroDocumento());
            authResponse.setTelefono(usuario.getPersona().getTelefono());
        }

        if ("ROLE_AGENCIA".equals(rolName)) {
            Optional<Agencia> agOpt = agenciaServicio.buscarPorIdUsuario(usuario.getIdUsuario());
            if (agOpt.isPresent()) {
                Agencia ag = agOpt.get();
                authResponse.setNombreComercial(ag.getNombreComercial());
                authResponse.setRazonSocial(ag.getRazonSocial());
                if (ag.getNombreComercial() != null && !ag.getNombreComercial().isBlank()) {
                    authResponse.setNombre(ag.getNombreComercial());
                } else if (ag.getRazonSocial() != null && !ag.getRazonSocial().isBlank()) {
                    authResponse.setNombre(ag.getRazonSocial());
                }
                if ((authResponse.getCorreo() == null || authResponse.getCorreo().isBlank()) && ag.getEmail() != null) {
                    authResponse.setCorreo(ag.getEmail());
                }
                if (authResponse.getTelefono() == null && ag.getTelefono() != null) {
                    authResponse.setTelefono(ag.getTelefono());
                }
            }
        }

        return ResponseEntity.ok(authResponse);
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
