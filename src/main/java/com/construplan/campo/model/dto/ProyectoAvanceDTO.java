package com.construplan.campo.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * DTO para agrupar e informar sobre el avance físico de metas de un proyecto.
 * Permite renderizar barras de progreso y porcentajes acumulados por obra en campo.
 */
@Getter
@Builder
@ToString
public class ProyectoAvanceDTO {
    private final int idProyecto;
    private final String nombreProyecto;
    private final int totalAsignadas;
    private final int totalCompletadas;
    private final double porcentajeAvance;
}
