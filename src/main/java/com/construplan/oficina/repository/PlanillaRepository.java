package com.construplan.oficina.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.construplan.oficina.model.entity.EstadoPlanilla;
import com.construplan.oficina.model.entity.Planilla;

@Repository
public interface PlanillaRepository extends JpaRepository<Planilla, Integer> {

    // Planillas filtradas por estado con datos del empleado precargados
    @Query("SELECT p FROM Planilla p JOIN FETCH p.empleado WHERE p.estado = :estado ORDER BY p.fechaInicio DESC")
    List<Planilla> findByEstadoOrderByFechaInicioDesc(@Param("estado") EstadoPlanilla estado);

    // Todas las planillas con empleado precargado
    @Query("SELECT p FROM Planilla p JOIN FETCH p.empleado ORDER BY p.fechaInicio DESC")
    List<Planilla> findAllWithEmpleado();

    // Historial de planillas de un empleado
    List<Planilla> findByEmpleado_IdEmpleadoOrderByFechaInicioDesc(int idEmpleado);

    // Conteo por estado para el dashboard
    long countByEstado(EstadoPlanilla estado);

    // Verificar duplicados para evitar generar planilla dos veces para el mismo empleado y periodo
    boolean existsByEmpleado_IdEmpleadoAndFechaInicioAndFechaFin(int idEmpleado, LocalDate fechaInicio, LocalDate fechaFin);

    // Suma del total por pagar para planillas generadas (pendientes de pago)
    @Query("SELECT COALESCE(SUM(p.totalPago), 0) FROM Planilla p WHERE p.estado = :estado")
    java.math.BigDecimal sumTotalPagoByEstado(@Param("estado") EstadoPlanilla estado);
}