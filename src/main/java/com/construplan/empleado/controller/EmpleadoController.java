package com.construplan.empleado.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

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

import com.construplan.campo.model.entity.AsignacionTarea;
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
    	  // Verificar si el empleado ya tiene registro en la tabla empleado
         Optional<Empleado> optionalEmpleado = empleadoService.buscarOptionalPorUsername(username);
         if (optionalEmpleado.isEmpty()) {
             return "redirect:/empleado/perfil";
         }

         Empleado empleado = optionalEmpleado.get();
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
        AsignacionTarea asignacionPendiente = null;
        var asignacionesHoy = asignacionTareaRepository.findByEmpleadoAndFechaRange(idEmpleado, hoy, hoy);
        if (!asignacionesHoy.isEmpty()) {
        	   AsignacionTarea asignacion = asignacionesHoy.get(0);
            puedeCompletarTarea = (asignacion.getHoraMetaCompletada() == null);
            if (puedeCompletarTarea) {
                asignacionPendiente = asignacion;
            }
        }

        model.addAttribute("empleado",         empleado);
        model.addAttribute("fechaActual",       hoy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("horasSemanales",    horasSemanales);
        model.addAttribute("horasExtras",       horasExtras);
        model.addAttribute("ticketsAbiertos",   0);// TODO: Implementar conteo de tickets cuando se integre TicketRepository
        model.addAttribute("estadoAsistencia",  estadoAsistencia);
        model.addAttribute("idRegistroActivo",  idRegistroActivo);
        model.addAttribute("puedeCompletarTarea", puedeCompletarTarea);
        model.addAttribute("asignacionPendiente", asignacionPendiente);
        model.addAttribute("ultimosRegistros",  registroDiarioService.obtenerUltimosRegistros(idEmpleado));
        return "empleado/dashboard";
    }

    
    // ─── Perfil del empleado ─────────────────────────────────────────────────

    @GetMapping("/perfil")
    public String mostrarPerfil(Authentication authentication, Model model) {
        String username = authentication.getName();
        Optional<Empleado> optionalEmpleado = empleadoService.buscarOptionalPorUsername(username);

     // Determinar si es primer registro o edición
        boolean esNuevoRegistro = optionalEmpleado.isEmpty();
        model.addAttribute("esNuevoRegistro", esNuevoRegistro);
        model.addAttribute("empleado", optionalEmpleado.orElse(null));
        return "empleado/perfil";
    }

    @PostMapping("/perfil")
    public String guardarPerfil(Authentication authentication,
    	     @RequestParam(required = false) String nombres,
             @RequestParam(required = false) String apellidos,
             @RequestParam(required = false) String dni,
                                @RequestParam(required = false) String direccion,
                                @RequestParam(required = false) String telefono,
                                @RequestParam(required = false) String fechaNacimiento,
                                @RequestParam(required = false) String banco,
                                @RequestParam(required = false) String cuentaBancaria,
                                RedirectAttributes redirectAttributes) {

        String username = authentication.getName();
        

        // Parsear fecha de nacimiento si se proporcionó
        LocalDate fechaNacimientoParsed = parsearFecha(fechaNacimiento);
        if (fechaNacimiento != null && !fechaNacimiento.isBlank() && fechaNacimientoParsed == null) {
            redirectAttributes.addFlashAttribute("error", "Formato de fecha inválido");
            return "redirect:/empleado/perfil";
        }

        Optional<Empleado> optionalEmpleado = empleadoService.buscarOptionalPorUsername(username);

        try {
            if (optionalEmpleado.isEmpty()) {
                // Primer registro: validar campos obligatorios
                if (nombres == null || nombres.isBlank()
                        || apellidos == null || apellidos.isBlank()
                        || dni == null || dni.isBlank()) {
                    redirectAttributes.addFlashAttribute("error",
                            "Nombres, apellidos y DNI son obligatorios");
                    return "redirect:/empleado/perfil";
                }

                empleadoService.crearRegistroInicial(
                        username, nombres.trim(), apellidos.trim(), dni.trim(),
                        direccion, telefono, fechaNacimientoParsed, banco, cuentaBancaria
                );
                redirectAttributes.addFlashAttribute("exito",
                        "¡Registro completado! Ya puedes acceder a tu dashboard");
            } else {
                // Edición de perfil existente
                empleadoService.actualizarPerfil(
                        optionalEmpleado.get().getIdEmpleado(),
                        direccion, telefono, fechaNacimientoParsed, banco, cuentaBancaria
                );
                redirectAttributes.addFlashAttribute("exito", "Datos actualizados correctamente");
            }
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/empleado/perfil";
        }

        return "redirect:/empleado/perfil";
    }

    // ─── Utilidades privadas ─────────────────────────────────────────────────

    private LocalDate parsearFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(fecha);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }
}