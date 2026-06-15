package com.construplan.oficina.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.construplan.empleado.model.entity.Empleado;
import com.construplan.empleado.repository.EmpleadoRepository;
import com.construplan.oficina.model.entity.PeriodoPago;
import com.construplan.oficina.model.entity.Sueldo;
import com.construplan.oficina.repository.SueldoRepository;

/**
 * Servicio encargado de gestionar los sueldos acordados individualmente
 * con los empleados, manteniendo la vigencia y coherencia del historial salarial.
 */
@Service
public class SueldoService {

    @Autowired
    private SueldoRepository sueldoRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    /**
     * Obtiene todo el historial de sueldos de un empleado ordenado por fecha de inicio descendente.
     */
    public List<Sueldo> obtenerHistorialSueldos(Integer idEmpleado) {
        return sueldoRepository.findByEmpleado_IdEmpleadoOrderByFechaInicioDesc(idEmpleado);
    }

    /**
     * Obtiene el sueldo activo actual de un empleado en la fecha de hoy.
     */
    public Optional<Sueldo> obtenerSueldoActivo(Integer idEmpleado) {
        return sueldoRepository.findSueldoActivoByEmpleadoAndFecha(idEmpleado, LocalDate.now());
    }

    /**
     * Registra un nuevo acuerdo salarial para un empleado.
     * Cierra el sueldo activo anterior si corresponde para evitar traslapes de fechas.
     */
    @Transactional
    public Sueldo registrarNuevoSueldo(Integer idEmpleado, BigDecimal monto, PeriodoPago periodo, LocalDate fechaInicio) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El sueldo debe ser mayor a cero");
        }
        if (fechaInicio == null) {
            throw new IllegalArgumentException("La fecha de inicio de vigencia es requerida");
        }

        Empleado empleado = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new IllegalStateException("Empleado no encontrado"));

        // Buscar si existe un sueldo activo a la fecha de inicio del nuevo sueldo
        Optional<Sueldo> sueldoActivoOpt = sueldoRepository.findSueldoActivoByEmpleadoAndFecha(idEmpleado, fechaInicio);

        if (sueldoActivoOpt.isPresent()) {
            Sueldo sueldoActivo = sueldoActivoOpt.get();
            
            // Si la fecha de inicio del nuevo sueldo es menor o igual a la del activo, hay conflicto de traslape
            if (!fechaInicio.isAfter(sueldoActivo.getFechaInicio())) {
                throw new IllegalStateException("La fecha de inicio debe ser posterior a la fecha de inicio del sueldo activo actual (" + sueldoActivo.getFechaInicio() + ")");
            }

            // Cerrar el sueldo anterior colocándole como fecha de fin el día previo al inicio del nuevo sueldo
            sueldoActivo.setFechaFin(fechaInicio.minusDays(1));
            sueldoRepository.save(sueldoActivo);
        }

        // Crear e insertar el nuevo acuerdo salarial
        Sueldo nuevoSueldo = Sueldo.builder()
                .empleado(empleado)
                .sueldo(monto)
                .periodo(periodo)
                .fechaInicio(fechaInicio)
                .fechaFin(null) // Vigencia indefinida hasta que se inserte otro sueldo
                .build();

        return sueldoRepository.save(nuevoSueldo);
    }
}
