package com.construplan.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario {
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@Column(name = "id_usuario")
	private Integer id; 
	
	@Column(unique = true)
	private String username; 
	
	private String password; 
	
	@Enumerated(EnumType.STRING)
	private Rol rol;
	
	private boolean activo;
	//private Timestamp fechaCreacion;
	 
	 //private Empleado empleado;
}
