package com.travelink.repositorio;

import com.travelink.entidades.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepositorio extends JpaRepository<Notificacion, Integer> {
    List<Notificacion> findByIdUsuarioOrderByFechaRegistroDesc(Integer idUsuario);
    List<Notificacion> findByRolTargetOrderByFechaRegistroDesc(String rolTarget);
    List<Notificacion> findByLeidoFalse();
}
