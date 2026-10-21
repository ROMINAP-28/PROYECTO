package com.travelink.repositorio;

import com.travelink.entidades.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ROL: DAO Principal (Spring Data JPA)
 * Responsabilidad: Operaciones CRUD y consultas simples sobre la entidad Notificacion.
 */
@Repository
public interface NotificacionRepositorio extends JpaRepository<Notificacion, Integer> {
    List<Notificacion> findByIdUsuarioOrderByFechaRegistroDesc(Integer idUsuario);
    List<Notificacion> findByRolTargetOrderByFechaRegistroDesc(String rolTarget);
    List<Notificacion> findByLeidoFalse();
}
