package com.travelink.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la API REST.
 * Garantiza que nunca se envíen stack traces ni detalles internos sensibles al cliente HTTP.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getResourcePath());
        Map<String, Object> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", "Recurso no encontrado: " + ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("status", "error");
        error.put("code", "FORBIDDEN");
        error.put("message", "Acceso restringido: no posee los permisos necesarios");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Fallo de validación en petición REST: {}", fieldErrors);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("code", "VALIDATION_ERROR");
        response.put("message", "Los datos enviados contienen errores de validación");
        response.put("errors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento inválido: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", ex.getMessage() != null ? ex.getMessage() : "Parámetro inválido");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        log.warn("Estado inválido en la operación: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", ex.getMessage() != null ? ex.getMessage() : "Operación no permitida en el estado actual");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        log.error("Excepción no controlada capturada por GlobalExceptionHandler: ", ex);
        Map<String, Object> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", "Ha ocurrido un error interno al procesar su solicitud. Por favor intente más tarde.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
