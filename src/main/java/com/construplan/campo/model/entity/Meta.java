package com.construplan.campo.model.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "meta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "tarea")
@EqualsAndHashCode(exclude = "tarea")
public class Meta {
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "id_meta")
	    private int idMeta;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "id_tarea", nullable = false)
	    private Tarea tarea;

	    @Column(name = "cantidad", columnDefinition = "DECIMAL(10,2)")
	    private BigDecimal cantidad;
	    
	    @Column(name = "horas_equivalentes", nullable = false)
	    private BigDecimal horasEquivalentes;

	    @Column(name = "es_libre")
	    private boolean libre;
}
