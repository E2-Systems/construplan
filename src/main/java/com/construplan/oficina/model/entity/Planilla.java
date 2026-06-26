package com.construplan.oficina.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

import com.construplan.empleado.model.entity.Empleado;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;



@Entity
@Table(name = "planilla")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"empleado", "detalles", "ajustes"})
@EqualsAndHashCode(exclude = {"empleado", "detalles", "ajustes"})
public class Planilla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_planilla")
    private int idPlanilla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "total_horas_base", precision = 6, scale = 2)
    private BigDecimal totalHorasBase;

    @Column(name = "total_horas_extra", precision = 6, scale = 2)
    private BigDecimal totalHorasExtra;

    @Column(name = "total_pago", precision = 10, scale = 2)
    private BigDecimal totalPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    @Builder.Default
    private EstadoPlanilla estado = EstadoPlanilla.GENERADA;

    @OneToMany(mappedBy = "planilla", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PlanillaDetalle> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "planilla", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AjustePlanilla> ajustes = new ArrayList<>();
    
    // Se calcula la suma acumulada de los pagos diarios para mantener el subtotal por horas intacto ante cualquier ajuste posterior.
    public BigDecimal getSubtotalPago() {
        if (detalles == null) {
            return BigDecimal.ZERO;
        }
        return detalles.stream()
                .map(PlanillaDetalle::getPagoDia)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
