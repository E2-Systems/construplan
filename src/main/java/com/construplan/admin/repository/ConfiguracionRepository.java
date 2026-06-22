package com.construplan.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.construplan.admin.model.entity.Configuracion;


/**
 * Repositorio de acceso a datos para la entidad Configuracion.
 */
@Repository
public interface ConfiguracionRepository extends JpaRepository<Configuracion, Integer> {
}
