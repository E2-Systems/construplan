package com.construplan.campo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.construplan.campo.model.entity.Tarea;

import java.util.List;
import java.util.Optional;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Integer> {

    Optional<Tarea> findByNombreIgnoreCase(String nombre);
    List<Tarea> findByNombreContainingIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}