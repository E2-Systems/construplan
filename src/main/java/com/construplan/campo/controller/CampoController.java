package com.construplan.campo.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.construplan.empleado.model.entity.EstadoRegistro;
import com.construplan.empleado.model.entity.EstadoTicket;
import com.construplan.empleado.service.RegistroDiarioService;
import com.construplan.empleado.service.TicketService;

@Controller
@RequestMapping("/campo")
public class CampoController {
	// RegistroDiarioService, TicketService, AsignacionTareaService
    // se inyectan cuando los implementes
	 @Autowired
	    private RegistroDiarioService registroDiarioService;
	 
	 @Autowired
	    private TicketService ticketService;
	 
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {

        LocalDate hoy = LocalDate.now();

        // Valores fijos hasta implementar los services
     // Conteo real de registros pendientes desde la base de datos
        long registrosPendientes = registroDiarioService.contarRegistrosPorEstado(EstadoRegistro.PENDIENTE);
        long ticketsAbiertos     = ticketService.countByStatus(EstadoTicket.ABIERTO) + ticketService.countByStatus(EstadoTicket.EN_REVISION);
        int empleadosActivos    = 0;
        int tareasSemanales     = 0;

        model.addAttribute("fechaActual",         hoy.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", new Locale("es", "PE"))));
        model.addAttribute("registrosPendientes", registrosPendientes);
        model.addAttribute("ticketsAbiertos",     ticketsAbiertos);
        model.addAttribute("empleadosActivos",    empleadosActivos);
        model.addAttribute("tareasSemanales",     tareasSemanales);

        return "campo/dashboard";
    }
}
