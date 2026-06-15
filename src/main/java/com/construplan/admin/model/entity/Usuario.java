package com.construplan.admin.model.entity;

import java.time.LocalDateTime;

import com.construplan.empleado.model.entity.Empleado;
import com.construplan.model.entity.Rol;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "empleado")   // excluye la relación
@EqualsAndHashCode(exclude = "empleado")
public class Usuario {
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@Column(name = "id_usuario")
	private Integer id; 
	
	@Column(unique = true, nullable = false)
	private String username; 
	
	@Column(name = "password", nullable = false)
	private String password; 
	
	@Enumerated(EnumType.STRING)
	private Rol rol;
	
	private boolean activo;
	
	 @Column(name = "fecha_creacion", updatable = false)
	private LocalDateTime fechaCreacion;
	 
	 @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
	    private Empleado empleado;
	
	 @PrePersist //Esto asegura que cada vez que se inserte un nuevo Usuario, se setee automáticamente la fecha.
	   protected void onCreate() {
	       fechaCreacion = LocalDateTime.now();
	    }
}
