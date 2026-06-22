package com.construplan.oficina.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.construplan.empleado.model.entity.Empleado;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa la asignación histórica y activa del sueldo de un empleado.
 * Mapea la tabla 'sueldo' para fines de cálculo y auditoría de planillas.
 */
@Entity
@Table(name = "sueldo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "empleado")
public class Sueldo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sueldo")
    private Integer idSueldo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "sueldo", nullable = false, precision = 10, scale = 2)
    private BigDecimal sueldo;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodo", nullable = false)
    private PeriodoPago periodo;
}
