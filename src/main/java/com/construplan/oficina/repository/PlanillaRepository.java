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
    
 // Listar planillas de una semana específica con carga de empleado
    @Query("SELECT p FROM Planilla p JOIN FETCH p.empleado WHERE p.fechaInicio = :fechaInicio ORDER BY p.empleado.apellidos ASC")
    List<Planilla> findByFechaInicioOrderByEmpleado_ApellidosAsc(@Param("fechaInicio") LocalDate fechaInicio);

    // Obtener las fechas de inicio únicas de todas las planillas registradas (para combos de filtros)
    @Query("SELECT DISTINCT p.fechaInicio FROM Planilla p ORDER BY p.fechaInicio DESC")
    List<LocalDate> findDistinctFechaInicio();

    // Suma total de horas base de todas las planillas de una semana determinada
    @Query("SELECT COALESCE(SUM(p.totalHorasBase), 0) FROM Planilla p WHERE p.fechaInicio = :fechaInicio")
    java.math.BigDecimal sumTotalHorasBaseByFechaInicio(@Param("fechaInicio") LocalDate fechaInicio);

    // Suma total de horas extra de todas las planillas de una semana determinada
    @Query("SELECT COALESCE(SUM(p.totalHorasExtra), 0) FROM Planilla p WHERE p.fechaInicio = :fechaInicio")
    java.math.BigDecimal sumTotalHorasExtraByFechaInicio(@Param("fechaInicio") LocalDate fechaInicio);

    // Suma del pago neto total de todas las planillas de una semana determinada
    @Query("SELECT COALESCE(SUM(p.totalPago), 0) FROM Planilla p WHERE p.fechaInicio = :fechaInicio")
    java.math.BigDecimal sumTotalPagoByFechaInicio(@Param("fechaInicio") LocalDate fechaInicio);

    // Conteo de planillas por estado para una semana determinada
    @Query("SELECT COUNT(p) FROM Planilla p WHERE p.fechaInicio = :fechaInicio AND p.estado = :estado")
    long countByFechaInicioAndEstado(@Param("fechaInicio") LocalDate fechaInicio, @Param("estado") EstadoPlanilla estado);

    // Conteo total de planillas para una semana determinada
    @Query("SELECT COUNT(p) FROM Planilla p WHERE p.fechaInicio = :fechaInicio")
    long countByFechaInicio(@Param("fechaInicio") LocalDate fechaInicio);
}