package com.construplan.empleado.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    
    // ─── Perfil del empleado ─────────────────────────────────────────────────

    @GetMapping("/perfil")
    public String mostrarPerfil(Authentication authentication, Model model) {
        String username = authentication.getName();
        Empleado empleado = empleadoService.buscarPorUsername(username);

        model.addAttribute("empleado", empleado);
        return "empleado/perfil";
    }

    @PostMapping("/perfil")
    public String guardarPerfil(Authentication authentication,
                                @RequestParam(required = false) String direccion,
                                @RequestParam(required = false) String telefono,
                                @RequestParam(required = false) String fechaNacimiento,
                                @RequestParam(required = false) String banco,
                                @RequestParam(required = false) String cuentaBancaria,
                                RedirectAttributes redirectAttributes) {

        String username = authentication.getName();
        Empleado empleado = empleadoService.buscarPorUsername(username);

        // Parsear fecha de nacimiento si se proporcionó
        LocalDate fechaNacimientoParsed = null;
        if (fechaNacimiento != null && !fechaNacimiento.isBlank()) {
            try {
                fechaNacimientoParsed = LocalDate.parse(fechaNacimiento);
            } catch (DateTimeParseException exception) {
                redirectAttributes.addFlashAttribute("error", "Formato de fecha inválido");
                return "redirect:/empleado/perfil";
            }
        }

        try {
            empleadoService.actualizarPerfil(
                    empleado.getIdEmpleado(),
                    direccion,
                    telefono,
                    fechaNacimientoParsed,
                    banco,
                    cuentaBancaria
            );
            redirectAttributes.addFlashAttribute("exito", "Datos actualizados correctamente");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }

        return "redirect:/empleado/perfil";
    }
}