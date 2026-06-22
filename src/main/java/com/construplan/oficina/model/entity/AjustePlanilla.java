package com.construplan.oficina.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.construplan.empleado.model.entity.Empleado;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "ajuste_planilla")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "planilla")
@EqualsAndHashCode(exclude = "planilla")
public class AjustePlanilla {
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "id_ajuste")
	    private int idAjuste;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "id_planilla", nullable = false)
	    private Planilla planilla;

	    @Enumerated(EnumType.STRING)
	    @Column(name = "tipo", nullable = false)
	    private TipoAjuste tipo;

	    @Column(name = "monto", nullable = false, precision = 8, scale = 2)
	    private BigDecimal monto;

	    @Column(name = "motivo", length = 150)
	    private String motivo;
}
