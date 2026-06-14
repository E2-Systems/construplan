package com.construplan.empleado.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.construplan.empleado.model.entity.RegistroDiario;

@Repository
public interface RegistroDiarioRepository extends JpaRepository<RegistroDiario, Integer> {

    Optional<RegistroDiario> findByAsignacion_IdAsignacion(int idAsignacion);

   
    @Query("SELECT r FROM RegistroDiario r JOIN FETCH r.asignacion WHERE r.asignacion.empleado.idEmpleado = :idEmpleado AND r.asignacion.fecha = :fecha")
    Optional<RegistroDiario> findByEmpleadoIdAndFecha(@Param("idEmpleado") int idEmpleado, @Param("fecha") LocalDate fecha);

    @Query("SELECT r FROM RegistroDiario r JOIN FETCH r.asignacion WHERE r.asignacion.empleado.idEmpleado = :idEmpleado ORDER BY r.asignacion.fecha DESC")
    List<RegistroDiario> findUltimosRegistrosPorEmpleado(@Param("idEmpleado") int idEmpleado);

    @Query("SELECT COALESCE(SUM(r.horasBase), 0.0) FROM RegistroDiario r WHERE r.asignacion.empleado.idEmpleado = :idEmpleado AND r.asignacion.fecha BETWEEN :inicio AND :fin AND r.estado = 'APROBADO'")
    double sumHorasBaseAprobadas(@Param("idEmpleado") int idEmpleado, @Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(r.horasExtra), 0.0) FROM RegistroDiario r WHERE r.asignacion.empleado.idEmpleado = :idEmpleado AND r.asignacion.fecha BETWEEN :inicio AND :fin AND r.estado = 'APROBADO'")
    double sumHorasExtraAprobadas(@Param("idEmpleado") int idEmpleado, @Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
}
