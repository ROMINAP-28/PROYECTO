package com.travelink.controlador;

import com.travelink.controlador.dto.AuthResponse;
import com.travelink.entidades.Agencia;
import com.travelink.entidades.SolicitudAgencia;
import com.travelink.entidades.Usuario;
import com.travelink.repositorio.AgenciaRepositorio;
import com.travelink.repositorio.SolicitudAgenciaRepositorio;
import com.travelink.repositorio.UsuarioRepositorio;
import com.travelink.servicio.LoginAttemptService;
import com.travelink.servicio.UsuarioServicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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

@RestController
@RequestMapping("/api")
public class AuthApiController {

    private final UsuarioServicio usuarioServicio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final AgenciaRepositorio agenciaRepositorio;
    private final SolicitudAgenciaRepositorio solicitudAgenciaRepositorio;
    private final LoginAttemptService loginAttemptService;
    private final PasswordEncoder passwordEncoder;

    public AuthApiController(UsuarioServicio usuarioServicio,
                             UsuarioRepositorio usuarioRepositorio,
                             AgenciaRepositorio agenciaRepositorio,
                             SolicitudAgenciaRepositorio solicitudAgenciaRepositorio,
                             LoginAttemptService loginAttemptService,
                             PasswordEncoder passwordEncoder) {
        this.usuarioServicio = usuarioServicio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.agenciaRepositorio = agenciaRepositorio;
        this.solicitudAgenciaRepositorio = solicitudAgenciaRepositorio;
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
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(Map.of("status", "error", "message", "Cuenta bloqueada temporalmente por 5 intentos fallidos. Intente nuevamente en 15 minutos."));
        }

        Optional<Usuario> usuarioOpt = usuarioServicio.buscarPorLogin(login);
        if (usuarioOpt.isEmpty()) {
            loginAttemptService.loginFailed(lockoutKey);
            int remaining = loginAttemptService.getRemainingAttempts(lockoutKey);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Usuario o contraseña incorrectos. Intentos restantes: " + remaining));
        }

        Usuario usuario = usuarioOpt.get();

        // Validar contraseña
        boolean passwordOk = usuarioServicio.verificarContrasena(rawPassword, usuario.getContrasena());
        if (!passwordOk) {
            loginAttemptService.loginFailed(lockoutKey);
            int remaining = loginAttemptService.getRemainingAttempts(lockoutKey);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Usuario o contraseña incorrectos. Intentos restantes: " + remaining));
        }

        // Si la contraseña estaba en texto plano o formato antiguo, rehashear automáticamente con BCrypt
        if (usuario.getContrasena() == null || !usuario.getContrasena().startsWith("$2")) {
            usuario.setContrasena(passwordEncoder.encode(rawPassword));
            usuarioRepositorio.save(usuario);
        }

        // Validar si el usuario está activo
        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
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
            Optional<Agencia> agenciaOpt = agenciaRepositorio.findByIdUsuario(usuario.getIdUsuario());
            List<SolicitudAgencia> solicitudes = solicitudAgenciaRepositorio.findByIdUsuario(usuario.getIdUsuario());

            boolean tieneSolicitudAceptada = solicitudes.stream()
                    .anyMatch(s -> "Aceptado".equalsIgnoreCase(s.getEstado()) || "Aceptada".equalsIgnoreCase(s.getEstado()) || "ACTIVO".equalsIgnoreCase(s.getEstado()));

            boolean agenciaActiva = agenciaOpt.isPresent() && "ACTIVO".equalsIgnoreCase(agenciaOpt.get().getEstado());

            if (!tieneSolicitudAceptada && !agenciaActiva) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("status", "error", "message", "Su solicitud de agencia aún no ha sido aprobada por el administrador."));
            }

            if (agenciaOpt.isPresent()) {
                idAgencia = agenciaOpt.get().getIdAgencia();
            }
        }

        // Éxito: limpiar intentos fallidos
        loginAttemptService.loginSucceeded(lockoutKey);

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

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping({"/auth/logout", "/logout"})
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
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
            usuario = usuarioRepositorio.findById(idUsuario).orElse(null);
        }
        if (usuario == null) {
            String username = auth.getName();
            usuario = usuarioRepositorio.buscarPorCorreoOUsuario(username).orElse(null);
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
            Optional<Agencia> ag = agenciaRepositorio.findByIdUsuario(usuario.getIdUsuario());
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
