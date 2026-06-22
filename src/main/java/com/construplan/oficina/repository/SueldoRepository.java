package com.construplan.oficina.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.construplan.oficina.model.entity.Sueldo;
/**
 * Repositorio de Spring Data JPA para la entidad Sueldo.
 * Contiene operaciones de persistencia e histórico para las tarifas de los empleados.
 */
@Repository
public interface SueldoRepository extends JpaRepository<Sueldo, Integer> {
	 /**
     * Busca los registros de sueldo de un empleado ordenados por fecha de inicio descendente.
     */
    List<Sueldo> findByEmpleado_IdEmpleadoOrderByFechaInicioDesc(Integer idEmpleado);

    /**
     * Busca el sueldo activo actual de un empleado en base a una fecha de referencia.
     * Un sueldo está activo si la fecha de referencia está dentro de [fechaInicio, fechaFin]
     * o si es posterior a fechaInicio y fechaFin es nula (vigencia indefinida).
     */
    @Query("SELECT s FROM Sueldo s WHERE s.empleado.idEmpleado = :idEmpleado " +
           "AND s.fechaInicio <= :fecha " +
           "AND (s.fechaFin IS NULL OR s.fechaFin >= :fecha)")
    Optional<Sueldo> findSueldoActivoByEmpleadoAndFecha(@Param("idEmpleado") Integer idEmpleado, @Param("fecha") LocalDate fecha);
}
