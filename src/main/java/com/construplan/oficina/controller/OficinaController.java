package com.construplan.oficina.controller;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// La verificación de sesión y rol la gestiona Spring Security en SecurityConfig (/oficina/** → ROLE_OFICINA)
@Controller
@RequestMapping("/oficina")
public class OficinaController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        // Fecha actual formateada en español-Perú
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "EEEE, dd 'de' MMMM 'de' yyyy", new Locale("es", "PE"));
        String fechaActual = hoy.format(formatter);

        // Hora actual formateada para mostrar en el encabezado
        String horaActual = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        // Datos estadísticos estáticos — se reemplazarán por consultas reales al implementar el módulo de planillas
        int planillasGeneradas = 8;
        int planillasPendientes = 3;
        int empleadosActivos = 45;
        double totalPorPagar = 38500.50;

        // Rango de la semana laboral actual (lunes a sábado)
        LocalDate inicioSemana = hoy.with(DayOfWeek.MONDAY);
        LocalDate finSemana = inicioSemana.plusDays(5);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String semanaActual = inicioSemana.format(dateFormatter) + " - " + finSemana.format(dateFormatter);

        // Contadores de alertas pendientes
        int ajustesPendientes = 2;
        int ticketsPendientes = 5;

        // Se inyectan todos los atributos al modelo para que Thymeleaf los renderice en la vista
        model.addAttribute("fechaActual", fechaActual);
        model.addAttribute("horaActual", horaActual);
        model.addAttribute("semanaActual", semanaActual);
        model.addAttribute("planillasGeneradas", planillasGeneradas);
        model.addAttribute("planillasPendientes", planillasPendientes);
        model.addAttribute("empleadosActivos", empleadosActivos);
        model.addAttribute("totalPorPagar", totalPorPagar);
        model.addAttribute("ajustesPendientes", ajustesPendientes);
        model.addAttribute("ticketsPendientes", ticketsPendientes);

        return "oficina/dashboard";
    }
}
