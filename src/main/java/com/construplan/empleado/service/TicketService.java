package com.construplan.empleado.service;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.construplan.empleado.model.entity.EstadoRegistro;
import com.construplan.empleado.model.entity.EstadoTicket;
import com.construplan.empleado.model.entity.RegistroDiario;
import com.construplan.empleado.model.entity.Ticket;
import com.construplan.empleado.repository.RegistroDiarioRepository;
import com.construplan.empleado.repository.TicketRepository;


/**
 * Servicio de negocio para gestionar el ciclo de vida de los tickets de inconformidad.
 * Proporciona el aislamiento transaccional y las reglas de negocio necesarias
 * para la creación y transición de estados del ticket.
 */
@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private RegistroDiarioRepository registroDiarioRepository;

    /**
     * Registra un nuevo ticket en estado ABIERTO vinculándolo a un registro diario de asistencia.
     * Se valida la existencia del registro diario para asegurar la consistencia referencial.
     */
    @Transactional
    public Ticket createTicket(int idRegistro, String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de la inconformidad es obligatorio.");
        }

        RegistroDiario registro = registroDiarioRepository.findById(idRegistro)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el registro diario con ID: " + idRegistro));

        Ticket ticket = Ticket.builder()
                .registroDiario(registro)
                .motivo(motivo)
                .estado(EstadoTicket.ABIERTO)
                .build();

        return ticketRepository.save(ticket);
    }

    /**
     * Recupera todos los tickets correspondientes a las asistencias de un empleado específico.
     */
    public List<Ticket> getTicketsByEmployee(int idEmpleado) {
        return ticketRepository.findByEmpleadoIdWithRegistroOrderByFechaCreacionDesc(idEmpleado);
    }

    /**
     * Retorna el número de tickets activos (abiertos o en revisión) de un empleado.
     * Se realiza para reflejar con precisión las alertas en el dashboard del empleado.
     */
    public long getOpenTicketCountByEmployee(int idEmpleado) {
        return ticketRepository.countByEstadoAndRegistroDiario_Asignacion_Empleado_IdEmpleado(EstadoTicket.ABIERTO, idEmpleado)
             + ticketRepository.countByEstadoAndRegistroDiario_Asignacion_Empleado_IdEmpleado(EstadoTicket.EN_REVISION, idEmpleado);
    }

    /**
     * Recupera todos los tickets pendientes de resolución (en estado ABIERTO o EN_REVISION).
     * Esto proporciona al Ingeniero de Campo la lista de discrepancias activas a resolver.
     */
    public List<Ticket> getAllPendingTickets() {
    	   return ticketRepository.findByEstadoNotWithRegistroOrderByFechaCreacionDesc(EstadoTicket.RESUELTO);
    }

    /**
     * Recupera el histórico total de tickets (abiertos, en revisión y resueltos).
     */
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAllWithRegistroOrderByFechaCreacionDesc();
    }

    /**
     * Obtiene un ticket por su identificador primario.
     * Lanza una excepción controlada si el recurso solicitado no existe.
     */
    public Ticket getTicket(int idTicket) {
        return ticketRepository.findById(idTicket)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el ticket con ID: " + idTicket));
    }

    /**
     * Cambia el estado del ticket a EN_REVISION para indicar que está bajo análisis.
     * Esta transición de estado es exclusiva del Ingeniero de Campo y previene dobles atenciones.
     */
    @Transactional
    public Ticket startReview(int idTicket) {
        Ticket ticket = getTicket(idTicket);
        if (ticket.getEstado() != EstadoTicket.ABIERTO) {
            throw new IllegalStateException("Solo se pueden revisar tickets en estado ABIERTO.");
        }
        ticket.setEstado(EstadoTicket.EN_REVISION);
        return ticketRepository.save(ticket);
    }

    /**
     * Resuelve definitivamente el ticket asignándole una respuesta de justificación y
       * pasando su estado a RESUELTO. Además, actualiza las horas de asistencia vinculada
     * y cambia su estado de OBSERVADO a APROBADO.
     * La respuesta es obligatoria para garantizar el sustento.
     */
    @Transactional
    public Ticket resolveTicket(int idTicket, String respuesta, BigDecimal horasBase, BigDecimal horasExtra) {
        if (respuesta == null || respuesta.trim().isEmpty()) {
            throw new IllegalArgumentException("La respuesta de resolución es obligatoria.");
        }

        Ticket ticket = getTicket(idTicket);
        if (ticket.getEstado() == EstadoTicket.RESUELTO) {
            throw new IllegalStateException("El ticket ya se encuentra RESUELTO.");
        }
        // Actualizamos la asistencia diaria vinculada con las nuevas horas corregidas y aprobamos
        RegistroDiario registro = ticket.getRegistroDiario();
        registro.setHorasBase(horasBase);
        registro.setHorasExtra(horasExtra);
        registro.setEstado(EstadoRegistro.APROBADO);
        registroDiarioRepository.save(registro);

        ticket.setRespuesta(respuesta);
        ticket.setEstado(EstadoTicket.RESUELTO);
        return ticketRepository.save(ticket);
    }

    /**
     * Retorna el conteo global de tickets por un estado determinado.
     * Se utiliza principalmente para poblar las métricas dinámicas en los tableros generales.
     */
    public long countByStatus(EstadoTicket estado) {
        return ticketRepository.countByEstado(estado);
    }
}
