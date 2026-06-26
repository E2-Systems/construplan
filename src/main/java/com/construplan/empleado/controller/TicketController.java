package com.construplan.empleado.controller;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.construplan.empleado.model.entity.Empleado;
import com.construplan.empleado.model.entity.RegistroDiario;
import com.construplan.empleado.model.entity.Ticket;
import com.construplan.empleado.service.EmpleadoService;
import com.construplan.empleado.service.RegistroDiarioService;
import com.construplan.empleado.service.TicketService;

/**
 * Controlador transversal para el módulo de Tickets (Inconformidades).
 * Centraliza las peticiones bajo la ruta '/tickets' y determina dinámicamente
 * la vista y acciones disponibles según el rol de la sesión activa,
 * cumpliendo con los requerimientos de seguridad y visualización.
 */
@Controller
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private RegistroDiarioService registroDiarioService;

    /**
     * Redirecciona las rutas de legado de empleados o campo hacia la ruta unificada.
     * Garantiza retrocompatibilidad con enlaces ya existentes en dashboards.
     */
    @GetMapping({"/empleado/tickets", "/campo/tickets"})
    public String redirectTickets(@RequestParam(value = "action", required = false) String action) {
        if ("new".equalsIgnoreCase(action)) {
            return "redirect:/tickets/nuevo";
        }
        return "redirect:/tickets";
    }

    /**
     * Muestra la bandeja de tickets según el rol del usuario autenticado.
     * - EMPLEADO: Solo visualiza sus propias discrepancias reportadas.
     * - CAMPO u OFICINA: Ven la bandeja de inconformidades, por defecto filtrada por pendientes.
     */
    @GetMapping("/tickets")
    public String list(Authentication authentication, 
                       @RequestParam(value = "filtro", required = false) String filtro, 
                       Model model) {
        boolean isEmpleado = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLEADO"));
        boolean isCampo = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CAMPO"));
        boolean isOficina = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OFICINA"));

        model.addAttribute("active", "tickets");

        if (isEmpleado) {
            String username = authentication.getName();
            Optional<Empleado> optionalEmpleado = empleadoService.buscarOptionalPorUsername(username);
            if (optionalEmpleado.isEmpty()) {
                return "redirect:/empleado/perfil";
            }
            Empleado empleado = optionalEmpleado.get();
            model.addAttribute("tickets", ticketService.getTicketsByEmployee(empleado.getIdEmpleado()));
            model.addAttribute("empleado", empleado);
          
            return "tickets/lista";
        } else if (isCampo || isOficina) {
            List<Ticket> tickets;
            if ("todos".equalsIgnoreCase(filtro)) {
                tickets = ticketService.getAllTickets();
            } else {
                tickets = ticketService.getAllPendingTickets();
            }
            model.addAttribute("tickets", tickets);
            model.addAttribute("filtro", filtro != null ? filtro : "pendientes");
            model.addAttribute("isOficina", isOficina);
            model.addAttribute("isCampo", isCampo);
          
            return "tickets/lista";
        }

        return "redirect:/login";
    }

    /**
     * Muestra el formulario para reportar una nueva inconformidad.
     * Carga las asistencias del empleado para que pueda seleccionar cuál desea reportar.
     */
    @GetMapping("/tickets/nuevo")
    public String newForm(Authentication authentication, Model model) {
        boolean isEmpleado = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLEADO"));
        if (!isEmpleado) {
            return "redirect:/tickets";
        }

        String username = authentication.getName();
        Optional<Empleado> optionalEmpleado = empleadoService.buscarOptionalPorUsername(username);
        if (optionalEmpleado.isEmpty()) {
            return "redirect:/empleado/perfil";
        }
        Empleado empleado = optionalEmpleado.get();

        List<RegistroDiario> registros = registroDiarioService.obtenerUltimosRegistros(empleado.getIdEmpleado());
        model.addAttribute("registros", registros);
        model.addAttribute("active", "tickets");
        return "tickets/nuevo";
    }

    /**
     * Procesa la creación de un nuevo ticket de inconformidad.
     * Solo es accesible por los empleados.
     */
    @PostMapping("/tickets/crear")
    public String create(Authentication authentication, 
                         @RequestParam("idRegistro") int idRegistro, 
                         @RequestParam("motivo") String motivo, 
                         RedirectAttributes redirectAttributes) {
        boolean isEmpleado = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLEADO"));
        if (!isEmpleado) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para crear un ticket.");
            return "redirect:/tickets";
        }

        try {
            ticketService.createTicket(idRegistro, motivo);
            redirectAttributes.addFlashAttribute("success", "Ticket de inconformidad creado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/tickets/nuevo";
        }
        return "redirect:/tickets";
    }

    /**
     * Visualiza los detalles completos de un ticket específico.
     * Incorpora una validación de seguridad (guard) para evitar que empleados visualicen tickets de otros.
     */
    @GetMapping("/tickets/{id}")
    public String viewDetail(Authentication authentication, 
                             @PathVariable("id") int id, 
                             Model model, 
                             RedirectAttributes redirectAttributes) {
        try {
            Ticket ticket = ticketService.getTicket(id);
            
            boolean isEmpleado = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLEADO"));
            boolean isCampo = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CAMPO"));
            boolean isOficina = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_OFICINA"));
            if (isEmpleado) {
                String username = authentication.getName();
                Optional<Empleado> optionalEmpleado = empleadoService.buscarOptionalPorUsername(username);
                if (optionalEmpleado.isEmpty() || 
                    optionalEmpleado.get().getIdEmpleado() != ticket.getRegistroDiario().getAsignacion().getEmpleado().getIdEmpleado()) {
                    redirectAttributes.addFlashAttribute("error", "No tienes acceso a este ticket.");
                    return "redirect:/tickets";
                }
            }

            model.addAttribute("ticket", ticket);
            model.addAttribute("active", "tickets");
            model.addAttribute("isEmpleado", isEmpleado);
            model.addAttribute("isCampo", isCampo);
            model.addAttribute("isOficina", isOficina);
           
            return "tickets/detalle";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/tickets";
        }
    }

    /**
     * Transiciona el ticket al estado EN_REVISION.
     * Esta acción está restringida exclusivamente a los Ingenieros de Campo.
     */
    @PostMapping("/tickets/{id}/revisar")
    public String startReview(Authentication authentication, 
                              @PathVariable("id") int id, 
                              RedirectAttributes redirectAttributes) {
        boolean isCampo = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CAMPO"));
        if (!isCampo) {
            redirectAttributes.addFlashAttribute("error", "Solo los Ingenieros de Campo pueden revisar tickets.");
            return "redirect:/tickets/" + id;
        }

        try {
            ticketService.startReview(id);
            redirectAttributes.addFlashAttribute("success", "Ticket marcado en revisión.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tickets/" + id;
    }

    /**
       * Resuelve la inconformidad aportando una justificación escrita obligatoria
     * y las horas modificadas/aprobadas para la asistencia.
     * Exclusivo para el Ingeniero de Campo.
     */
    @PostMapping("/tickets/{id}/resolver")
    public String resolve(Authentication authentication, 
                          @PathVariable("id") int id, 
                          @RequestParam("respuesta") String respuesta, 
                          @RequestParam("horasBase") BigDecimal horasBase,
                          @RequestParam("horasExtra") BigDecimal horasExtra,
                          RedirectAttributes redirectAttributes) {
        boolean isCampo = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CAMPO"));
        if (!isCampo) {
            redirectAttributes.addFlashAttribute("error", "Solo los Ingenieros de Campo pueden resolver tickets.");
            return "redirect:/tickets/" + id;
        }

        try {
            ticketService.resolveTicket(id, respuesta, horasBase, horasExtra);
            redirectAttributes.addFlashAttribute("success", "Ticket resuelto correctamente y asistencia diaria vinculada aprobada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tickets/" + id;
    }
}
