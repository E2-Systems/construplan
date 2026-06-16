package com.construplan.oficina.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.construplan.oficina.model.entity.AjustePlanilla;

@Repository
public interface AjustePlanillaRepository extends JpaRepository<AjustePlanilla, Integer> {

    // Obtiene los ajustes de una planilla específica
    List<AjustePlanilla> findByPlanilla_IdPlanilla(int idPlanilla);

    // Cuenta los ajustes asociados a planillas con un estado determinado
    long countByPlanilla_Estado(com.construplan.oficina.model.entity.EstadoPlanilla estado);
}
