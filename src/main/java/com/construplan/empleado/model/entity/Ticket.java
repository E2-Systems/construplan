package com.construplan.empleado.model.entity;

import java.time.LocalDate;
import com.construplan.empleado.model.entity.RegistroDiario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidad que representa una inconformidad reportada por un empleado.
 * Mapea a la tabla 'ticket' y vincula una discrepancia a un registro diario
 * específico para permitir la trazabilidad y la resolución por parte del personal de campo.
 */
@Entity
@Table(name = "ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ticket")
    private Integer idTicket;

    /**
     * Vinculación obligatoria al registro de asistencia diario.
     * Es EAGER porque al consultar el ticket siempre se requiere mostrar
     * los detalles de la asistencia cuestionada.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_registro", nullable = false)
    private RegistroDiario registroDiario;

    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion;

    @Column(name = "motivo", length = 200, nullable = false)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @Builder.Default
    private EstadoTicket estado = EstadoTicket.ABIERTO;

    @Column(name = "respuesta", columnDefinition = "TEXT")
    private String respuesta;

    /**
     * Garantiza que la fecha de creación se registre automáticamente en la persistencia.
     */
    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDate.now();
        }
    }
}
