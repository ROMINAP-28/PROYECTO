package com.travelink.controlador;

import com.travelink.controlador.dto.AuthResponse;
import com.travelink.entidades.Persona;
import com.travelink.entidades.Usuario;
import com.travelink.servicio.AgenciaServicio;
import com.travelink.servicio.LoginAttemptService;
import com.travelink.servicio.UsuarioServicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthApiControllerTest {

    @Mock
    private UsuarioServicio usuarioServicio;

    @Mock
    private AgenciaServicio agenciaServicio;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AuthApiController authApiController;

    private Usuario usuarioTurista;

    @BeforeEach
    void setUp() {
        usuarioTurista = new Usuario();
        usuarioTurista.setIdUsuario(10);
        usuarioTurista.setNombreUsuario("turista1");
        usuarioTurista.setContrasena("$2a$10$hashedPasswordSample");
        usuarioTurista.setEstado("ACTIVO");
        usuarioTurista.setIdRol(1); // TURISTA

        Persona persona = new Persona();
        persona.setNombre("Juan");
        persona.setApellidoPaterno("Pérez");
        persona.setApellidoMaterno("Gómez");
        persona.setEmail("juan@example.com");
        persona.setNroDocumento("12345678");
        usuarioTurista.setPersona(persona);
    }

    @Test
    @DisplayName("Login con credenciales correctas debe retornar HTTP 200 y AuthResponse")
    void testLoginExitoso() {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getSession(true)).thenReturn(session);
        when(loginAttemptService.isBlocked(anyString())).thenReturn(false);
        when(usuarioServicio.buscarPorLogin("turista1")).thenReturn(Optional.of(usuarioTurista));
        when(usuarioServicio.verificarContrasena("MiPasswordSegura", "$2a$10$hashedPasswordSample")).thenReturn(true);

        Map<String, String> credentials = new HashMap<>();
        credentials.put("usuario", "turista1");
        credentials.put("password", "MiPasswordSegura");

        ResponseEntity<?> responseEntity = authApiController.login(credentials, request, response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof AuthResponse);

        AuthResponse authResponse = (AuthResponse) responseEntity.getBody();
        assertEquals(10, authResponse.getIdUsuario());
        assertEquals("ROLE_TURISTA", authResponse.getRol());
        assertEquals("juan@example.com", authResponse.getCorreo());

        verify(loginAttemptService, times(1)).loginSucceeded(anyString());
    }

    @Test
    @DisplayName("Login con contraseña incorrecta debe retornar HTTP 401 Unauthorized")
    void testLoginContrasenaIncorrecta() {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(loginAttemptService.isBlocked(anyString())).thenReturn(false);
        when(usuarioServicio.buscarPorLogin("turista1")).thenReturn(Optional.of(usuarioTurista));
        when(usuarioServicio.verificarContrasena("PasswordErronea", "$2a$10$hashedPasswordSample")).thenReturn(false);
        when(loginAttemptService.getRemainingAttempts(anyString())).thenReturn(4);

        Map<String, String> credentials = new HashMap<>();
        credentials.put("usuario", "turista1");
        credentials.put("password", "PasswordErronea");

        ResponseEntity<?> responseEntity = authApiController.login(credentials, request, response);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        verify(loginAttemptService, times(1)).loginFailed(anyString());
    }

    @Test
    @DisplayName("Login cuando la cuenta o IP está bloqueada por 5 intentos fallidos debe retornar HTTP 423 LOCKED")
    void testLoginBloqueadoPorIntentosFallidos() {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(loginAttemptService.isBlocked(anyString())).thenReturn(true);

        Map<String, String> credentials = new HashMap<>();
        credentials.put("usuario", "turista1");
        credentials.put("password", "CualquierClave");

        ResponseEntity<?> responseEntity = authApiController.login(credentials, request, response);

        assertEquals(HttpStatus.LOCKED, responseEntity.getStatusCode());
        verify(usuarioServicio, never()).buscarPorLogin(anyString());
    }

    @Test
    @DisplayName("Login sin ingresar credenciales debe retornar HTTP 400 Bad Request")
    void testLoginCamposVacios() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("usuario", "");
        credentials.put("password", "");

        ResponseEntity<?> responseEntity = authApiController.login(credentials, request, response);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }
}
