package com.travelink.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public class SecurityUtils {

    public static Optional<HttpSession> getCurrentSession() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest req = attrs.getRequest();
            return Optional.ofNullable(req.getSession(false));
        }
        return Optional.empty();
    }

    public static Optional<Integer> getAuthenticatedIdUsuario() {
        Optional<HttpSession> session = getCurrentSession();
        if (session.isPresent()) {
            Object id = session.get().getAttribute("ID_USUARIO");
            if (id instanceof Integer) {
                return Optional.of((Integer) id);
            }
        }
        return Optional.empty();
    }

    public static Optional<Integer> getAuthenticatedIdAgencia() {
        Optional<HttpSession> session = getCurrentSession();
        if (session.isPresent()) {
            Object id = session.get().getAttribute("ID_AGENCIA");
            if (id instanceof Integer) {
                return Optional.of((Integer) id);
            }
        }
        return Optional.empty();
    }

    public static Optional<String> getAuthenticatedRol() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            for (GrantedAuthority ga : auth.getAuthorities()) {
                return Optional.of(ga.getAuthority());
            }
        }
        return Optional.empty();
    }

    public static boolean hasRole(String role) {
        String targetRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            for (GrantedAuthority ga : auth.getAuthorities()) {
                if (ga.getAuthority().equalsIgnoreCase(targetRole)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }
}
