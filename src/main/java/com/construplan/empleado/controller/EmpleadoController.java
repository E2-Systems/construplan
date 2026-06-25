package com.construplan.empleado.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.construplan.campo.model.entity.AsignacionTarea;
import com.construplan.campo.repository.AsignacionTareaRepository;
import com.construplan.empleado.model.entity.Empleado;
import com.construplan.empleado.model.entity.RegistroDiario;
import com.construplan.empleado.service.EmpleadoService;
import com.construplan.empleado.service.RegistroDiarioService;
import com.construplan.empleado.service.TicketService;
import com.construplan.oficina.model.entity.Planilla;
import com.construplan.oficina.service.PlanillaService;

@Controller
@RequestMapping("/empleado")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private RegistroDiarioService registroDiarioService;
    

    @Autowired
    private AsignacionTareaRepository asignacionTareaRepository;
    
    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private PlanillaService planillaService;

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
        
        List<RegistroDiario> todosRegistros = registroDiarioService.obtenerUltimosRegistros(idEmpleado);
        List<RegistroDiario> ultimos3 = todosRegistros.stream()
                .limit(3)
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("empleado",         empleado);
        model.addAttribute("fechaActual",       hoy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("horasSemanales",    horasSemanales);
        model.addAttribute("horasExtras",       horasExtras);
        model.addAttribute("ticketsAbiertos", ticketService.getOpenTicketCountByEmployee(idEmpleado));
        model.addAttribute("ultimosRegistros", ultimos3);
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
    /**
     * Permite al empleado visualizar la asignación de su jornada del día de hoy
     * y registrar su asistencia diaria (reloj marcador de entrada, meta y salida).
     */
    @GetMapping("/jornada")
    public String viewJornada(Authentication authentication, Model model) {
        String username = authentication.getName();
        Optional<Empleado> optionalEmpleado = empleadoService.buscarOptionalPorUsername(username);
        if (optionalEmpleado.isEmpty()) {
            return "redirect:/empleado/perfil";
        }
        Empleado empleado = optionalEmpleado.get();
        int idEmpleado = empleado.getIdEmpleado();
        LocalDate hoy = LocalDate.now();

        // Evaluar el estado de la asistencia del día de hoy
        String estadoAsistencia = "SIN_ENTRADA";
        int idRegistroActivo = -1;

        var activo = registroDiarioService.obtenerRegistroActivoHoy(idEmpleado, hoy);
        if (activo != null) {
            estadoAsistencia = "EN_TURNO";
            idRegistroActivo = activo.getIdRegistro();
        } else if (registroDiarioService.tieneRegistroHoy(idEmpleado, hoy)) {
            estadoAsistencia = "JORNADA_COMPLETA";
        }

        // Determinar si puede reportar su tarea como completada y obtener la asignación pendiente
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

        model.addAttribute("empleado", empleado);
        model.addAttribute("active", "jornada");
        model.addAttribute("fechaActual", hoy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("estadoAsistencia", estadoAsistencia);
        model.addAttribute("idRegistroActivo", idRegistroActivo);
        model.addAttribute("puedeCompletarTarea", puedeCompletarTarea);
        model.addAttribute("asignacionPendiente", asignacionPendiente);

        return "empleado/jornada";
    }
    
    /**
     * Muestra el historial completo de asistencia del empleado logueado.
       * Soporta los alias de ruta '/registro-horas' y '/horas'.
     */
    @GetMapping({"/registro-horas", "/horas"})
    public String viewRecords(Authentication authentication, Model model) {
        String username = authentication.getName();
        Optional<Empleado> optionalEmpleado = empleadoService.buscarOptionalPorUsername(username);
        if (optionalEmpleado.isEmpty()) {
            return "redirect:/empleado/perfil";
        }
        Empleado empleado = optionalEmpleado.get();
        model.addAttribute("empleado", empleado);
        model.addAttribute("active", "horas");
        model.addAttribute("registros", registroDiarioService.obtenerUltimosRegistros(empleado.getIdEmpleado()));
        return "empleado/registro-horas";
    }

    /**
     * Muestra el historial completo de planillas y pagos del empleado logueado.
     * Soporta los alias de ruta '/planillas' y '/pago'.
     */
    @GetMapping({"/planillas", "/pago"})
    public String viewPayrolls(Authentication authentication, Model model) {
        String username = authentication.getName();
        Optional<Empleado> optionalEmpleado = empleadoService.buscarOptionalPorUsername(username);
        if (optionalEmpleado.isEmpty()) {
            return "redirect:/empleado/perfil";
        }
        Empleado empleado = optionalEmpleado.get();
        model.addAttribute("empleado", empleado);
        model.addAttribute("active", "pago");
        model.addAttribute("planillas", planillaService.getPayrollsByEmployee(empleado.getIdEmpleado()));
        return "empleado/planillas";
    }

    /**
     * Muestra el detalle de una planilla específica (recibo de pago digital) del empleado.
     * Incorpora una validación de seguridad (guard) para evitar el acceso a planillas ajenas.
     */
    @GetMapping("/planillas/{id}")
    public String viewPayrollDetail(Authentication authentication, 
                                    @PathVariable("id") int idPlanilla, 
                                    Model model, 
                                    RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        Optional<Empleado> optionalEmpleado = empleadoService.buscarOptionalPorUsername(username);
        if (optionalEmpleado.isEmpty()) {
            return "redirect:/empleado/perfil";
        }
        Empleado empleado = optionalEmpleado.get();

        Optional<Planilla> optionalPlanilla = planillaService.getPayroll(idPlanilla);
        if (optionalPlanilla.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "La planilla no existe.");
            return "redirect:/empleado/planillas";
        }
        Planilla planilla = optionalPlanilla.get();

        // Guard de seguridad: Validar que la planilla pertenezca al empleado autenticado
        if (planilla.getEmpleado().getIdEmpleado() != empleado.getIdEmpleado()) {
            redirectAttributes.addFlashAttribute("error", "No tienes acceso a este recibo de pago.");
            return "redirect:/empleado/planillas";
        }

        model.addAttribute("empleado", empleado);
        model.addAttribute("planilla", planilla);
        model.addAttribute("active", "pago");
        return "empleado/planilla-detalle";
    }
}