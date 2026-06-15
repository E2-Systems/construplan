package com.construplan.empleado.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.construplan.campo.model.entity.AsignacionTarea;
import com.construplan.campo.repository.AsignacionTareaRepository;
import com.construplan.empleado.model.entity.EstadoRegistro;
import com.construplan.empleado.model.entity.RegistroDiario;
import com.construplan.empleado.repository.RegistroDiarioRepository;

@Service
public class RegistroDiarioService {

    @Autowired
    private RegistroDiarioRepository registroDiarioRepository;

    @Autowired
    private AsignacionTareaRepository asignacionTareaRepository;

    @Transactional
    public RegistroDiario registrarEntrada(int idEmpleado) {
        LocalDate hoy = LocalDate.now();

        // Se verifica que el empleado tenga una asignación de trabajo para hoy
        AsignacionTarea asignacion = asignacionTareaRepository.findByEmpleadoAndFechaRange(idEmpleado, hoy, hoy)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No tienes una tarea asignada para hoy. Pide a tu supervisor que registre tu asignación de hoy."));

        // Se verifica si ya existe un registro de asistencia para esa asignación
        Optional<RegistroDiario> registroExistente = registroDiarioRepository
                .findByAsignacion_IdAsignacion(asignacion.getIdAsignacion());

        if (registroExistente.isPresent() && registroExistente.get().getHoraInicio() != null) {
            throw new IllegalStateException("Ya has registrado tu entrada para el día de hoy.");
        }

        RegistroDiario registro = registroExistente.orElseGet(() -> RegistroDiario.builder()
                .asignacion(asignacion)
                .estado(EstadoRegistro.PENDIENTE)
                .build());

        registro.setHoraInicio(LocalTime.now());
        return registroDiarioRepository.save(registro);
    }

    @Transactional
    public RegistroDiario registrarSalida(int idEmpleado) {
        LocalDate hoy = LocalDate.now();

        // Se obtiene el registro diario del empleado para el día de hoy
        RegistroDiario registro = registroDiarioRepository.findByEmpleadoIdAndFecha(idEmpleado, hoy)
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró un registro de entrada. Debes registrar tu entrada primero."));

        if (registro.getHoraInicio() == null) {
            throw new IllegalStateException("Debes registrar tu entrada primero.");
        }

        if (registro.getHoraFin() != null) {
            throw new IllegalStateException("Ya has registrado tu salida para el día de hoy.");
        }

        registro.setHoraFin(LocalTime.now());

        // Cálculo de horas trabajadas base (límite de 8 horas) y extras
        double totalHoras = registro.getHorasTrabajadas();
        double base = Math.min(totalHoras, 8.0);
        double extra = Math.max(0.0, totalHoras - 8.0);

        registro.setHorasBase(BigDecimal.valueOf(base));
        registro.setHorasExtra(BigDecimal.valueOf(extra));

        return registroDiarioRepository.save(registro);
    }

    @Transactional
    public RegistroDiario completarTarea(int idEmpleado) {
        LocalDate hoy = LocalDate.now();

        // Se obtiene la asignación del empleado para el día de hoy
        AsignacionTarea asignacion = asignacionTareaRepository.findByEmpleadoAndFechaRange(idEmpleado, hoy, hoy)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No tienes una tarea asignada para hoy."));

        asignacion.setHoraMetaCompletada(LocalTime.now());
        asignacionTareaRepository.save(asignacion);

        // Se obtiene o crea el registro de asistencia asociado
        RegistroDiario registro = registroDiarioRepository.findByAsignacion_IdAsignacion(asignacion.getIdAsignacion())
                .orElseGet(() -> RegistroDiario.builder()
                        .asignacion(asignacion)
                        .estado(EstadoRegistro.PENDIENTE)
                        .build());

        return registroDiarioRepository.save(registro);
    }

    public double obtenerTotalHorasSemanales(int idEmpleado) {
        LocalDate hoy = LocalDate.now();
        LocalDate lunes = hoy.with(DayOfWeek.MONDAY);
        LocalDate domingo = lunes.plusDays(6);
        return registroDiarioRepository.sumHorasBaseAprobadas(idEmpleado, lunes, domingo);
    }

    public double obtenerTotalHorasExtrasSemanales(int idEmpleado) {
        LocalDate hoy = LocalDate.now();
        LocalDate lunes = hoy.with(DayOfWeek.MONDAY);
        LocalDate domingo = lunes.plusDays(6);
        return registroDiarioRepository.sumHorasExtraAprobadas(idEmpleado, lunes, domingo);
    }

    public RegistroDiario obtenerRegistroActivoHoy(int idEmpleado, LocalDate hoy) {
        return registroDiarioRepository.findByEmpleadoIdAndFecha(idEmpleado, hoy)
                .filter(r -> r.getHoraInicio() != null && r.getHoraFin() == null)
                .orElse(null);
    }

    public boolean tieneRegistroHoy(int idEmpleado, LocalDate hoy) {
        return registroDiarioRepository.findByEmpleadoIdAndFecha(idEmpleado, hoy).isPresent();
    }

    public List<RegistroDiario> obtenerUltimosRegistros(int idEmpleado) {
        return registroDiarioRepository.findUltimosRegistrosPorEmpleado(idEmpleado);
    }

    // Obtiene registros filtrados por estado para la vista de validación
    public List<RegistroDiario> obtenerRegistrosPorEstado(EstadoRegistro estado) {
        return registroDiarioRepository.findByEstado(estado);
    }

    // Conteo rápido de registros por estado para indicadores del dashboard
    public long contarRegistrosPorEstado(EstadoRegistro estado) {
        return registroDiarioRepository.countByEstado(estado);
    }

    // Aprueba un registro diario cambiando su estado a APROBADO
    @Transactional
    public RegistroDiario aprobarRegistro(int idRegistro) {
        RegistroDiario registro = registroDiarioRepository.findById(idRegistro)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el registro con ID: " + idRegistro));

        registro.setEstado(EstadoRegistro.APROBADO);
        return registroDiarioRepository.save(registro);
    }

    // Marca un registro como observado para revisión posterior
    @Transactional
    public RegistroDiario observarRegistro(int idRegistro) {
        RegistroDiario registro = registroDiarioRepository.findById(idRegistro)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el registro con ID: " + idRegistro));

        registro.setEstado(EstadoRegistro.OBSERVADO);
        return registroDiarioRepository.save(registro);
    }
}
