package com.construplan.oficina.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "planilla_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "planilla")
@EqualsAndHashCode(exclude = "planilla")
public class PlanillaDetalle {
	  @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "id_detalle")
	    private int idDetalle;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "id_planilla", nullable = false)
	    private Planilla planilla;

	    @Column(name = "fecha", nullable = false)
	    private LocalDate fecha;

	    @Column(name = "horas_base", precision = 4, scale = 2)
	    private BigDecimal horasBase;

	    @Column(name = "horas_extra", precision = 4, scale = 2)
	    private BigDecimal horasExtra;

	    @Column(name = "pago_dia", precision = 8, scale = 2)
	    private BigDecimal pagoDia;
}
