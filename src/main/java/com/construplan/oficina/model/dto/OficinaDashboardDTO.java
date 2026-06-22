package com.construplan.oficina.model.dto;


import java.util.List;
import com.construplan.oficina.model.entity.Planilla;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * DTO para unificar todos los indicadores y datos del dashboard de oficina.
 * Encapsula la información temporal y estadísticas agregadas para mantener el
 * controlador limpio y cohesivo.
 */
@Getter
@Builder
@ToString
public class OficinaDashboardDTO {
    private final String fechaActual;
    private final String horaActual;
    private final String semanaActual;
    private final long planillasGeneradas;
    private final long planillasPendientes;
    private final int empleadosActivos;
    private final double totalPorPagar;
    private final long ajustesPendientes;
    private final long ticketsPendientes;
    private final List<Planilla> planillasRecientes;
}