package com.construplan.empleado.model.entity;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.construplan.campo.model.entity.AsignacionTarea;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "registro_diario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro")
    private int idRegistro;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_asignacion", nullable = false)
    private AsignacionTarea asignacion;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Column(name = "produccion_real", precision = 10, scale = 2)
    private BigDecimal produccionReal;

    @Column(name = "horas_base", precision = 4, scale = 2)
    private BigDecimal horasBase;

    @Column(name = "horas_extra", precision = 4, scale = 2)
    private BigDecimal horasExtra;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    @Builder.Default
    private EstadoRegistro estado = EstadoRegistro.PENDIENTE;

    // Método de utilidad para calcular las horas trabajadas entre inicio y fin
    public double getHorasTrabajadas() {
        if (horaInicio == null || horaFin == null) {
            return 0.0;
        }
        long duracionMinutos = java.time.Duration.between(horaInicio, horaFin).toMinutes();
        return (double) duracionMinutos / 60.0;
    }
}