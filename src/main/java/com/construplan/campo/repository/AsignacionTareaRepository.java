package com.construplan.campo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.construplan.campo.model.entity.AsignacionTarea;

@Repository
public interface AsignacionTareaRepository extends JpaRepository<AsignacionTarea, Integer> {
	 List<AsignacionTarea> findByFecha(LocalDate fecha);

	    List<AsignacionTarea> findByEmpleadoIdEmpleado(int idEmpleado);

	    List<AsignacionTarea> findByProyectoIdProyecto(int idProyecto);

	    @Query("SELECT a FROM AsignacionTarea a WHERE a.empleado.idEmpleado = :idEmpleado " +
	           "AND a.fecha BETWEEN :inicio AND :fin")
	    List<AsignacionTarea> findByEmpleadoAndFechaRange(
	            @Param("idEmpleado") int idEmpleado,
	            @Param("inicio") LocalDate inicio,
	            @Param("fin") LocalDate fin);

	    List<AsignacionTarea> findByAsignadorId(int id);

	    boolean existsByEmpleadoIdEmpleadoAndFecha(int idEmpleado, LocalDate fecha);
}
