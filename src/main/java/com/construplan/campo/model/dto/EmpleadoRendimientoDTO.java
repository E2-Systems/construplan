package com.construplan.campo.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * DTO para representar el rendimiento individual de los empleados en campo.
 * Mide el porcentaje de metas físicas finalizadas sobre el total asignadas.
 */
@Getter
@Builder
@ToString
public class EmpleadoRendimientoDTO {
    private final String nombreCompleto;
    private final int tareasAsignadas;
    private final int tareasCompletadas;
    private final double porcentajeRendimiento;
}
