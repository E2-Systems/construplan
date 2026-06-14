package com.construplan.empleado.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.construplan.admin.model.entity.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "empleado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "usuario")   // excluye la relación
@EqualsAndHashCode(exclude = "usuario")
public class Empleado {
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@Column(name = "id_empleado")
	private Integer idEmpleado; 
	
	 @OneToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "id_usuario", nullable = true)
	private Usuario usuario; // ⚠️ NULLABLE - puede ser NULL si no tiene usuario
	 
	  @Column(name = "nombres", nullable = false)
	    private String nombres;

	    @Column(name = "apellidos", nullable = false)
	    private String apellidos;

	@Column(name="dni", columnDefinition="CHAR(8)")
	private String dni;
	private String categoria;
	private String direccion;
	private String telefono;
	 @Column(name = "fecha_nacimiento")
	    private LocalDate fechaNacimiento;
	private String banco;
	@Column(name = "cuenta_bancaria")
    private String cuentaBancaria;
	
	private boolean activo;
	
	// @Column(name = "fecha_registro", updatable = false)
	 //   private LocalDateTime fechaRegistro;

	 // Calculado — no es columna
    public boolean isTieneAccesoSistema() {
        return this.usuario != null;
    }

    // Nombre completo — útil en vistas
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
    
	
}
