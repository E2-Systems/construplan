package com.construplan.campo.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.construplan.admin.model.entity.Proyecto;
import com.construplan.empleado.model.entity.Empleado;
import com.construplan.model.entity.Usuario;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "asignacion_tarea")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"empleado", "asignador", "proyecto", "meta"})
@EqualsAndHashCode(exclude = {"empleado", "asignador", "proyecto", "meta"})
public class AsignacionTarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion")
    private int idAsignacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_asignador", nullable = false)
    private Usuario asignador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_meta", nullable = false)
    private Meta meta;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad")
    private Modalidad modalidad;

    @Column(name = "hora_meta_completada")
    private LocalTime horaMetaCompletada;

    // Acceso directo a la tarea a través de la meta
    public Tarea getTarea() {
        return meta != null ? meta.getTarea() : null;
    }
}