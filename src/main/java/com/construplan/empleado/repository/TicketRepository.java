package com.construplan.empleado.repository;


import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.construplan.empleado.model.entity.EstadoTicket;
import com.construplan.empleado.model.entity.Ticket;



/**
 * Repositorio JPA para acceder a los datos de la entidad Ticket.
 * Define consultas optimizadas con JOIN FETCH para evitar problemas de N+1
 * al cargar relaciones del registro diario, la asignación y el empleado.
 */
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    /**
     * Recupera todos los tickets ordenados por fecha de creación de forma descendente.
     * Realiza un fetch join de la relación hasta el empleado para mostrar los datos en
     * los listados administrativos eficientemente.
     */
    @Query("SELECT t FROM Ticket t JOIN FETCH t.registroDiario rd JOIN FETCH rd.asignacion a JOIN FETCH a.empleado e ORDER BY t.fechaCreacion DESC")
    List<Ticket> findAllWithRegistroOrderByFechaCreacionDesc();

    /**
     * Recupera los tickets de un empleado específico usando fetch joins para
     * optimizar la renderización de la lista del empleado.
     */
    @Query("SELECT t FROM Ticket t JOIN FETCH t.registroDiario rd JOIN FETCH rd.asignacion a JOIN FETCH a.empleado e WHERE e.idEmpleado = :idEmpleado ORDER BY t.fechaCreacion DESC")
    List<Ticket> findByEmpleadoIdWithRegistroOrderByFechaCreacionDesc(@Param("idEmpleado") int idEmpleado);

    /**
     * Recupera todos los tickets que no están resueltos (abiertos o en revisión)
     * Recupera todos los tickets que no tienen un estado específico (ej. RESUELTO)
     * para la bandeja de gestión de Ingenieros de Campo.
     */
    @Query("SELECT t FROM Ticket t JOIN FETCH t.registroDiario rd JOIN FETCH rd.asignacion a JOIN FETCH a.empleado e WHERE t.estado <> :estado ORDER BY t.fechaCreacion DESC")
    List<Ticket> findByEstadoNotWithRegistroOrderByFechaCreacionDesc(@Param("estado") EstadoTicket estado);
    /**
     * Cuenta cuántos tickets hay en un estado específico de forma global.
     * Útil para los KPIs de la oficina técnica y del campo.
     */
    long countByEstado(EstadoTicket estado);

    /**
     * Cuenta cuántos tickets de un estado específico posee un empleado.
     * Principalmente usado en el dashboard de empleado para indicar inconformidades pendientes.
     */
    long countByEstadoAndRegistroDiario_Asignacion_Empleado_IdEmpleado(EstadoTicket estado, int idEmpleado);
}
