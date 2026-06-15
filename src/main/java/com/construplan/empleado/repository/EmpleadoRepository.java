package com.construplan.empleado.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.construplan.empleado.model.entity.Categoria;
import com.construplan.empleado.model.entity.Empleado;
@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
	 // buscarPorDni
    Optional<Empleado> findByDni(String dni);
 // buscarPorIdUsuario
    Optional<Empleado> findByUsuario_Id(Integer idUsuario);
    @Query("SELECT e FROM Empleado e JOIN FETCH e.usuario WHERE e.usuario.username = :username")
    Optional<Empleado> findByUsuario_Username(@Param("username") String username);
    
 // buscarPorCategoria
    List<Empleado> findByCategoria(Categoria categoria);

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
