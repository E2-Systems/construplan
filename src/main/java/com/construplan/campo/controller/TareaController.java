package com.construplan.campo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.construplan.campo.model.entity.Tarea;
import com.construplan.campo.service.TareaService;

@Controller
@RequestMapping("/campo/tareas")
public class TareaController {
	 @Autowired
	    private TareaService tareaService;

	    // ── GET /campo/tareas ─────────────────────────────────────────────────────
	    @GetMapping
	    public String listar(Model model) {
	        model.addAttribute("tareas", tareaService.listarTodas());
	        return "campo/tareas";
	    }

	    // ── GET /campo/tareas/nueva ───────────────────────────────────────────────
	    @GetMapping("/nueva")
	    public String formularioCrear(Model model) {
	        model.addAttribute("tarea", new Tarea());
	        model.addAttribute("action", "create");
	        return "campo/tarea-form";
	    }

	    // ── GET /campo/tareas/{id}/editar ─────────────────────────────────────────
	    @GetMapping("/{id}/editar")
	    public String formularioEditar(@PathVariable int id, Model model,
	                                   RedirectAttributes redirectAttrs) {
	        try {
	            model.addAttribute("tarea", tareaService.obtenerPorId(id));
	            model.addAttribute("action", "update");
	            return "campo/tarea-form";
	        } catch (IllegalStateException e) {
	            redirectAttrs.addFlashAttribute("error", e.getMessage());
	            return "redirect:/campo/tareas";
	        }
	    }

	    // ── POST /campo/tareas ────────────────────────────────────────────────────
	    @PostMapping
	    public String crear(@ModelAttribute Tarea tarea, Model model,
	                        RedirectAttributes redirectAttrs) {
	        try {
	            tareaService.registrarTarea(tarea);
	            redirectAttrs.addFlashAttribute("mensaje", "Tarea creada exitosamente");
	            return "redirect:/campo/tareas";
	        } catch (IllegalArgumentException | IllegalStateException e) {
	            model.addAttribute("error", e.getMessage());
	            model.addAttribute("tarea", tarea);
	            model.addAttribute("action", "create");
	            return "campo/tarea-form";
	        }
	    }

	    // ── POST /campo/tareas/{id} ───────────────────────────────────────────────
	    @PostMapping("/{id}")
	    public String actualizar(@PathVariable int id, @ModelAttribute Tarea tarea,
	                             Model model, RedirectAttributes redirectAttrs) {
	        tarea.setIdTarea(id);
	        try {
	            tareaService.actualizarTarea(tarea);
	            redirectAttrs.addFlashAttribute("mensaje", "Tarea actualizada exitosamente");
	            return "redirect:/campo/tareas";
	        } catch (IllegalArgumentException | IllegalStateException e) {
	            model.addAttribute("error", e.getMessage());
	            model.addAttribute("tarea", tarea);
	            model.addAttribute("action", "update");
	            return "campo/tarea-form";
	        }
	    }

	    // ── POST /campo/tareas/{id}/eliminar ──────────────────────────────────────
	    @PostMapping("/{id}/eliminar")
	    public String eliminar(@PathVariable int id, RedirectAttributes redirectAttrs) {
	        try {
	            tareaService.eliminarTarea(id);
	            redirectAttrs.addFlashAttribute("mensaje", "Tarea eliminada exitosamente");
	        } catch (IllegalArgumentException | IllegalStateException e) {
	            redirectAttrs.addFlashAttribute("error", e.getMessage());
	        }
	        return "redirect:/campo/tareas";
	    }
}
