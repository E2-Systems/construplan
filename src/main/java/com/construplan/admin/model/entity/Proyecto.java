package com.construplan.admin.model.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.construplan.campo.model.entity.AsignacionTarea;
import com.construplan.campo.model.entity.Meta;
import com.construplan.campo.model.entity.Modalidad;
import com.construplan.empleado.model.entity.Empleado;
import com.construplan.model.entity.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "proyecto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proyecto {
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "id_proyecto")
	    private int idProyecto;

	    @Column(name = "nombre", nullable = false)
	    private String nombre;

	    @Column(name = "descripcion")
	    private String descripcion;

	    @Column(name = "activo")
	    private boolean activo;
}
