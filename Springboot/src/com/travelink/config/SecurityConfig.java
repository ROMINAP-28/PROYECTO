package com.travelink.config;

import com.travelink.servicio.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF Repository con Cookie HttpOnly=false para permitir lectura desde js/comun/api.js
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        tokenRepository.setCookiePath("/");

        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        // Desactiva BREACH token XOR para que el token de la cookie XSRF-TOKEN coincida directamente con el header X-XSRF-TOKEN
        requestHandler.setCsrfRequestAttributeName(null);

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .csrfTokenRepository(tokenRepository)
                .csrfTokenRequestHandler(requestHandler)
                .ignoringRequestMatchers(
                    "/api/auth/login",
                    "/api/auth/registro",
                    "/api/login",
                    "/api/registro",
                    "/api/agencia/login",
                    "/api/agencia/registro",
                    "/api/admin/login",
                    "/api/solicitudes/**",
                    "/api/guardar_reserva",
                    "/api/turista/reservas",
                    "/api/reservas",
                    "/api/reservas/**",
                    "/api/obtener_reservas",
                    "/api/cancelar_reserva"
                )
            )
            .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation().migrateSession()
            )
            .authorizeHttpRequests(auth -> auth
                // 1. Recursos estáticos públicos
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/favicon.ico"
                ).permitAll()

                // 2. Páginas de login y registro de cada rol
                .requestMatchers(
                    "/html/turista/login.html",
                    "/html/turista/registro.html",
                    "/html/administrador/login.html",
                    "/html/agencia/R_agencia_login.html",
                    "/html/agencia/R_agencia_registro.html",
                    "/html/agencia/R_agencia_empresa.html",
                    "/html/agencia/R_agencia_turismo.html",
                    "/html/agencia/R_agencia_responsable.html",
                    "/html/agencia/R_agencia_documentos.html",
                    "/html/agencia/R_agencia_revision.html"
                ).permitAll()

                // 3. Vistas públicas del turista
                .requestMatchers(
                    "/html/turista/index.html",
                    "/html/turista/detalle.html",
                    "/html/turista/experiencias.html",
                    "/html/turista/agencias.html",
                    "/html/turista/ofertas.html",
                    "/html/turista/nosotros.html",
                    "/html/turista/detalle-agencia.html",
                    "/html/turista/form_reserva.html"
                ).permitAll()

                // 4. APIs públicas sin sesión (GET de tours, destinos, calificaciones, solicitudes y endpoint de reservas que maneja su propia validación de sesión/identidad)
                .requestMatchers(
                    "/api/auth/**",
                    "/api/login",
                    "/api/registro",
                    "/api/agencia/login",
                    "/api/agencia/registro",
                    "/api/admin/login",
                    "/api/solicitudes/**",
                    "/api/guardar_reserva",
                    "/api/turista/reservas",
                    "/api/reservas",
                    "/api/reservas/**",
                    "/api/obtener_reservas",
                    "/api/cancelar_reserva"
                ).permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/destinos/**",
                    "/api/destinos",
                    "/api/tours/**",
                    "/api/tours",
                    "/api/calificaciones/**",
                    "/api/calificar/**",
                    "/api/calificar",
                    "/api/agencias/**",
                    "/api/agencias"
                ).permitAll()

                // 5. Panel y API de Administrador: solo ADMIN
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/html/administrador/**").hasRole("ADMIN")

                // 6. Portal y API de Agencia: solo AGENCIA
                .requestMatchers("/api/agencia/**").hasRole("AGENCIA")
                .requestMatchers("/html/agencia/**").hasRole("AGENCIA")

                // 7. Área protegida de Turista, Reservas y Comprobantes (accesible por cualquier usuario autenticado)
                .requestMatchers(
                    "/api/turista/reservas/*/comprobante.pdf",
                    "/api/turista/reservas/comprobante.pdf",
                    "/api/reservas/*/comprobante.pdf"
                ).hasAnyRole("TURISTA", "AGENCIA", "ADMIN")
                .requestMatchers("/html/turista/reservas.html").authenticated()
                .requestMatchers("/api/turista/**").authenticated()

                // 8. Cualquier otra petición debe estar autenticada
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(customAuthenticationEntryPoint())
                .accessDeniedHandler(customAccessDeniedHandler())
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .cacheControl(cache -> cache.disable())
            );

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            String uri = request.getRequestURI();
            if (uri.startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"status\":\"error\",\"code\":\"UNAUTHORIZED\",\"error\":\"Debe iniciar sesión para realizar esta acción\",\"message\":\"Debe iniciar sesión para realizar esta acción\"}");
            } else if (uri.startsWith("/html/administrador/")) {
                response.sendRedirect("/html/administrador/login.html");
            } else if (uri.startsWith("/html/agencia/")) {
                response.sendRedirect("/html/agencia/R_agencia_login.html");
            } else if (uri.startsWith("/html/turista/reservas.html") || uri.startsWith("/html/turista/form_reserva.html")) {
                String qs = request.getQueryString() != null ? "?" + request.getQueryString() : "";
                response.sendRedirect("/html/turista/login.html?redirect=" + java.net.URLEncoder.encode(uri + qs, "UTF-8"));
            } else {
                response.sendRedirect("/html/turista/login.html");
            }
        };
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            String uri = request.getRequestURI();
            if (uri.startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"status\":\"error\",\"code\":\"FORBIDDEN\",\"error\":\"Acceso restringido: no posee los permisos necesarios\",\"message\":\"Acceso restringido: no posee los permisos necesarios\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write("""
                    <!DOCTYPE html>
                    <html lang="es">
                    <head><meta charset="UTF-8"><title>403 Acceso Denegado</title>
                    <style>body{font-family:sans-serif;text-align:center;padding:50px;background:#f8fafc;color:#1e293b;}
                    h1{font-size:48px;color:#ef4444;}p{font-size:18px;}
                    a{color:#196f3d;font-weight:bold;text-decoration:none;}</style></head>
                    <body><h1>403</h1><h2>Acceso Restringido</h2>
                    <p>No posees los permisos necesarios para acceder a este recurso.</p>
                    <a href="/index.html">Volver a la página principal</a></body></html>
                    """);
            }
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "X-XSRF-TOKEN"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Filtro para forzar la carga del token diferido y enviarlo como cookie XSRF-TOKEN en cada petición GET
    private static class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                // Forzar lectura del token para que se escriba la cookie
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }
}
