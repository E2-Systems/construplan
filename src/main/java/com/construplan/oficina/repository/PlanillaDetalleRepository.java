package com.construplan.oficina.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.construplan.oficina.model.entity.PlanillaDetalle;

@Repository
public interface PlanillaDetalleRepository extends JpaRepository<PlanillaDetalle, Integer> {

    // Obtiene los detalles diarios de una planilla específica ordenados cronológicamente
    List<PlanillaDetalle> findByPlanilla_IdPlanillaOrderByFechaAsc(int idPlanilla);
}
