package com.construplan.admin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.construplan.admin.model.entity.Proyecto;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Integer> {

    List<Proyecto> findByActivoTrue();
    List<Proyecto> findByActivoFalse();
    Optional<Proyecto> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}
