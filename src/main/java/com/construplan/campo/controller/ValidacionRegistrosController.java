package com.construplan.campo.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.construplan.empleado.model.entity.EstadoRegistro;
import com.construplan.empleado.model.entity.RegistroDiario;
import com.construplan.empleado.service.RegistroDiarioService;


@Controller
@RequestMapping("/campo/validacion-registros")
public class ValidacionRegistrosController {

    @Autowired
    private RegistroDiarioService registroDiarioService;

    // Muestra la lista de registros pendientes de validación
    @GetMapping
    public String listarRegistrosPendientes(Model model) {
        List<RegistroDiario> registrosPendientes = registroDiarioService
                .obtenerRegistrosPorEstado(EstadoRegistro.PENDIENTE);

        model.addAttribute("registros", registrosPendientes);
        model.addAttribute("totalPendientes", registrosPendientes.size());

        return "campo/validacion-registros";
    }

    // Cambia el estado del registro a APROBADO
    @PostMapping("/{idRegistro}/aprobar")
    public String aprobarRegistro(
            @PathVariable int idRegistro,
            RedirectAttributes redirectAttributes) {

        try {
            registroDiarioService.aprobarRegistro(idRegistro);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Registro #" + idRegistro + " aprobado correctamente.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("mensajeError", exception.getMessage());
        }

        return "redirect:/campo/validacion-registros";
    }

    // Cambia el estado del registro a OBSERVADO
    @PostMapping("/{idRegistro}/observar")
    public String observarRegistro(
            @PathVariable int idRegistro,
            RedirectAttributes redirectAttributes) {

        try {
            registroDiarioService.observarRegistro(idRegistro);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Registro #" + idRegistro + " marcado como observado.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("mensajeError", exception.getMessage());
        }

        return "redirect:/campo/validacion-registros";
    }
}
