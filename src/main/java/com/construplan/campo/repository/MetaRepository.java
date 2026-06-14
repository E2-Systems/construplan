package com.construplan.campo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.construplan.campo.model.entity.Meta;

public interface MetaRepository extends JpaRepository<Meta, Integer> {

    List<Meta> findByTareaIdTarea(int idTarea);
    boolean existsByTareaIdTareaAndLibreTrue(int idTarea);
}