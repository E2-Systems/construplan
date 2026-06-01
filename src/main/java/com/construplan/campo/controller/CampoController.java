package com.construplan.campo.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/campo")
public class CampoController {
	// RegistroDiarioService, TicketService, AsignacionTareaService
    // se inyectan cuando los implementes

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {

        LocalDate hoy = LocalDate.now();

        // Valores fijos hasta implementar los services
        int registrosPendientes = 54;
        int ticketsAbiertos     = 0;
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
