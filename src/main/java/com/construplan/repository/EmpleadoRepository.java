package com.construplan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.construplan.model.entity.Empleado;
@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
	 // buscarPorDni
    Optional<Empleado> findByDni(String dni);
 // buscarPorIdUsuario
    Optional<Empleado> findByUsuario_Id(Integer idUsuario);
    Optional<Empleado> findByUsuario_Username(String username);
    
 // buscarPorCategoria
    List<Empleado> findByCategoria(String categoria);

    // listarActivos / listarInactivos
    List<Empleado> findByActivoTrue();
    List<Empleado> findByActivoFalse();

    // listarEmpleadosSinUsuario / listarEmpleadosConUsuario
    List<Empleado> findByUsuarioIsNull();
    List<Empleado> findByUsuarioIsNotNull();

    // contarEmpleadosActivos
    long countByActivoTrue();

    // contarEmpleadosSinUsuario / contarEmpleadosConUsuario
    long countByUsuarioIsNull();
    long countByUsuarioIsNotNull();
}
