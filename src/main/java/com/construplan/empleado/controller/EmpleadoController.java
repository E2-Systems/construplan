package com.construplan.empleado.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.construplan.campo.repository.AsignacionTareaRepository;
import com.construplan.empleado.model.entity.Empleado;
import com.construplan.empleado.service.EmpleadoService;
import com.construplan.empleado.service.RegistroDiarioService;

@Controller
@RequestMapping("/empleado")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private RegistroDiarioService registroDiarioService;
    

    @Autowired
    private AsignacionTareaRepository asignacionTareaRepository;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
    	
    	 String username = authentication.getName(); 

        // Obtener empleado desde el username autenticado — ya no viene de sesión manual
        Empleado empleado = empleadoService.buscarPorUsername(username);
        int idEmpleado = empleado.getIdEmpleado();
        LocalDate hoy = LocalDate.now();

        // Datos del dashboard
        double horasSemanales  = registroDiarioService.obtenerTotalHorasSemanales(idEmpleado);
        double horasExtras     = registroDiarioService.obtenerTotalHorasExtrasSemanales(idEmpleado);
        String estadoAsistencia = "SIN_ENTRADA";
        int idRegistroActivo   = -1;

        var activo = registroDiarioService.obtenerRegistroActivoHoy(idEmpleado, hoy);
        if (activo != null) {
            estadoAsistencia = "EN_TURNO";
            idRegistroActivo = activo.getIdRegistro();
        } else if (registroDiarioService.tieneRegistroHoy(idEmpleado, hoy)) {
           estadoAsistencia = "JORNADA_COMPLETA";
        }
        
     // Determinar si puede reportar su tarea como completada (tiene tarea hoy y aún no tiene hora de fin/completada)
        boolean puedeCompletarTarea = false;
        var asignacionesHoy = asignacionTareaRepository.findByEmpleadoAndFechaRange(idEmpleado, hoy, hoy);
        if (!asignacionesHoy.isEmpty()) {
            var asignacion = asignacionesHoy.get(0);
            puedeCompletarTarea = (asignacion.getHoraMetaCompletada() == null);
        }

        model.addAttribute("empleado",         empleado);
        model.addAttribute("fechaActual",       hoy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("horasSemanales",    horasSemanales);
        model.addAttribute("horasExtras",       horasExtras);
        model.addAttribute("ticketsAbiertos",   0);// TODO: Implementar conteo de tickets cuando se integre TicketRepository
        model.addAttribute("estadoAsistencia",  estadoAsistencia);
        model.addAttribute("idRegistroActivo",  idRegistroActivo);
        model.addAttribute("ultimosRegistros",  registroDiarioService.obtenerUltimosRegistros(idEmpleado));
        return "empleado/dashboard";
    }
}