package com.travelink.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

/**
 * Servicio de métricas de negocio para TRAVELINK utilizando Micrometer.
 * Expone contadores personalizados para Prometheus y dashboards de Grafana:
 * - travelink.reservas.creadas.total
 * - travelink.reservas.canceladas.total
 * - travelink.auth.logins.fallidos.total
 * - travelink.auth.logins.exitosos.total
 */
@Service
public class MetricasService {

    private final Counter reservasCreadasCounter;
    private final Counter reservasCanceladasCounter;
    private final Counter loginsFallidosCounter;
    private final Counter loginsExitososCounter;

    public MetricasService(MeterRegistry registry) {
        this.reservasCreadasCounter = Counter.builder("travelink.reservas.creadas.total")
                .description("Total de reservas turísticas creadas en la plataforma")
                .register(registry);

        this.reservasCanceladasCounter = Counter.builder("travelink.reservas.canceladas.total")
                .description("Total de reservas canceladas en la plataforma")
                .register(registry);

        this.loginsFallidosCounter = Counter.builder("travelink.auth.logins.fallidos.total")
                .description("Total de intentos fallidos de autenticación")
                .register(registry);

        this.loginsExitososCounter = Counter.builder("travelink.auth.logins.exitosos.total")
                .description("Total de inicios de sesión exitosos")
                .register(registry);
    }

    public void incrementarReservasCreadas() {
        reservasCreadasCounter.increment();
    }

    public void incrementarReservasCanceladas() {
        reservasCanceladasCounter.increment();
    }

    public void incrementarLoginsFallidos() {
        loginsFallidosCounter.increment();
    }

    public void incrementarLoginsExitosos() {
        loginsExitososCounter.increment();
    }
}
