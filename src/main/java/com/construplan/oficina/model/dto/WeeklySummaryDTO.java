package com.construplan.oficina.model.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * DTO para agrupar las métricas agregadas globales de una semana de planillas.
 * Se utiliza para evitar pasar múltiples variables independientes al modelo de Thymeleaf
 * y centralizar la representación de los totales acumulados.
 */
@Getter
@Builder
@ToString
public class WeeklySummaryDTO {
    private final BigDecimal totalBaseHours;
    private final BigDecimal totalExtraHours;
    private final BigDecimal totalPayment;
    private final long totalPayrolls;
    private final long paidPayrolls;
    private final long pendingPayrolls;
}