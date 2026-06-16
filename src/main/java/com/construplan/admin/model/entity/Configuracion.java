package com.construplan.admin.model.entity;


import java.math.BigDecimal;
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

/**
 * Entidad JPA que representa la tabla 'configuracion' en la base de datos.
 * Almacena los parámetros generales de negocio editables por el Administrador.
 */
@Entity
@Table(name = "configuracion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Configuracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_configuracion")
    private int idConfiguracion;

    @Column(name = "jornada_estandar", precision = 4, scale = 2, nullable = false)
    private BigDecimal jornadaEstandar;

    @Column(name = "multiplicador_horas_extras", precision = 4, scale = 2, nullable = false)
    private BigDecimal multiplicadorHorasExtras;

    @Column(name = "divisor_semanal", precision = 4, scale = 2, nullable = false)
    private BigDecimal divisorSemanal;

    @Column(name = "divisor_mensual", precision = 4, scale = 2, nullable = false)
    private BigDecimal divisorMensual;
}
