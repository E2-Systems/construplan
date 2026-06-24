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

        // Obtener historial ordenado de forma descendente por fecha de inicio (el último acuerdo estará primero)
        List<Sueldo> sueldos = sueldoRepository.findByEmpleado_IdEmpleadoOrderByFechaInicioDesc(idEmpleado);

        // Guard temprano: Si es el primer acuerdo del empleado, se crea con vigencia indefinida
        if (sueldos.isEmpty()) {
            Sueldo primerSueldo = Sueldo.builder()
                    .empleado(empleado)
                    .sueldo(monto)
                    .periodo(periodo)
                    .fechaInicio(fechaInicio)
                    .fechaFin(null)
                    .build();
            return sueldoRepository.save(primerSueldo);
        }

        Sueldo ultimoSueldo = sueldos.get(0);
            
        if (fechaInicio.equals(ultimoSueldo.getFechaInicio())) {
            ultimoSueldo.setSueldo(monto);
            ultimoSueldo.setPeriodo(periodo);
            return sueldoRepository.save(ultimoSueldo);
        }

        // Guard temprano: Prohibir registros con fecha anterior al último acuerdo para prevenir traslapes e inconsistencias
        if (fechaInicio.isBefore(ultimoSueldo.getFechaInicio())) {
            throw new IllegalStateException("La fecha de inicio de vigencia (" + fechaInicio 
                    + ") no puede ser anterior a la del acuerdo salarial actual (" + ultimoSueldo.getFechaInicio() + ").");
            }

        // Si la fecha es posterior, cerramos el acuerdo actual el día previo al inicio del nuevo sueldo
        ultimoSueldo.setFechaFin(fechaInicio.minusDays(1));
        sueldoRepository.save(ultimoSueldo);

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
    
    /**
     * Elimina un acuerdo salarial específico y reconstruye en cadena el historial
     * de vigencias del empleado para no dejar huecos ni periodos solapados.
     */
    @Transactional
    public int eliminarSueldo(int idSueldo) {
        Sueldo sueldoAEliminar = sueldoRepository.findById(idSueldo)
                .orElseThrow(() -> new IllegalArgumentException("Acuerdo salarial no encontrado con ID: " + idSueldo));

        Empleado empleado = sueldoAEliminar.getEmpleado();
        int idEmpleado = empleado.getIdEmpleado();
        sueldoRepository.delete(sueldoAEliminar);

        // Obtener los sueldos restantes ordenados de forma descendente (más recientes primero)
        List<Sueldo> historial = sueldoRepository.findByEmpleado_IdEmpleadoOrderByFechaInicioDesc(idEmpleado);

        // Guard temprano: si no quedan sueldos, no hay nada que reconstruir
        if (historial.isEmpty()) {
            return idEmpleado;
        }

        // Reconstrucción secuencial en cascada
        for (int i = 0; i < historial.size(); i++) {
            Sueldo sueldo = historial.get(i);
            if (i == 0) {
                // El acuerdo más reciente en la lista debe tener vigencia indefinida
                sueldo.setFechaFin(null);
            } else {
                // Los acuerdos históricos intermedios terminan el día previo al inicio del sueldo que les sucede
                Sueldo sueldoSiguiente = historial.get(i - 1);
                sueldo.setFechaFin(sueldoSiguiente.getFechaInicio().minusDays(1));
            }
            sueldoRepository.save(sueldo);
        }
        return idEmpleado;
    }
    public List<Sueldo> listarTodos() {
        return sueldoRepository.findAll();
    }

}
