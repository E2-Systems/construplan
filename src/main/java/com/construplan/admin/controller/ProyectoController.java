package com.construplan.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.construplan.admin.model.entity.Proyecto;
import com.construplan.admin.service.ProyectoService;
/**
 * Controlador de Spring MVC para la administración de proyectos por parte del Administrador.
 * Gestiona operaciones de visualización, creación, modificación, activación y eliminación.
 */

@Controller
@RequestMapping("/admin/proyectos")
public class ProyectoController {
	  @Autowired
	    private ProyectoService proyectoService;

	    @GetMapping
	    public String list(Model model) {
	        model.addAttribute("proyectos", proyectoService.listarTodos());
	        return "admin/proyectos";
	    }

	    @GetMapping("/nuevo")
	    public String formCreate(Model model) {
	        model.addAttribute("proyecto", new Proyecto());
	        model.addAttribute("action", "create");
	        return "admin/proyecto-form";
	    }

	    @GetMapping("/{id}/editar")
	    public String formEdit(@PathVariable int id, Model model,
	                                   RedirectAttributes redirectAttrs) {
	        try {
	            model.addAttribute("proyecto", proyectoService.obtenerPorId(id));
	            model.addAttribute("action", "update");
	            return "admin/proyecto-form";
	        } catch (IllegalStateException exception) {
	            redirectAttrs.addFlashAttribute("error", exception.getMessage());
	            return "redirect:/admin/proyectos";
	        }
	    }

	    @PostMapping
	    public String create(@ModelAttribute Proyecto proyecto, Model model,
	                        RedirectAttributes redirectAttrs) {
	        try {
	            proyectoService.guardar(proyecto);
	            redirectAttrs.addFlashAttribute("mensaje", "Proyecto creado exitosamente");
	            return "redirect:/admin/proyectos";
	        } catch (IllegalArgumentException | IllegalStateException exception) {
	            model.addAttribute("error", exception.getMessage());
	            model.addAttribute("proyecto", proyecto);
	            model.addAttribute("action", "create");
	            return "admin/proyecto-form";
	        }
	    }

	    @PostMapping("/{id}")
	    public String update(@PathVariable int id,
	                             @ModelAttribute Proyecto proyecto,
	                             Model model, RedirectAttributes redirectAttrs) {
	        proyecto.setIdProyecto(id);
	        try {
	            proyectoService.actualizar(proyecto);
	            redirectAttrs.addFlashAttribute("mensaje", "Proyecto actualizado exitosamente");
	            return "redirect:/admin/proyectos";
	        } catch (IllegalArgumentException | IllegalStateException exception) {
	            model.addAttribute("error", exception.getMessage());	       
	            model.addAttribute("proyecto", proyecto);
	            model.addAttribute("action", "update");
	            return "admin/proyecto-form";
	        }
	    }

	    @PostMapping("/{id}/estado")
	    public String changeStatus(@PathVariable int id,
	                                @RequestParam boolean activo,
	                                RedirectAttributes redirectAttrs) {
	        try {
	            proyectoService.activarDesactivar(id, activo);
	            redirectAttrs.addFlashAttribute("mensaje",
	                    activo ? "Proyecto activado" : "Proyecto desactivado");
	        } catch (IllegalStateException exception) {
	            redirectAttrs.addFlashAttribute("error", exception.getMessage());
	        }
	        return "redirect:/admin/proyectos";
	    }

	    @PostMapping("/{id}/eliminar")
	    public String delete(@PathVariable int id, RedirectAttributes redirectAttrs) {
	        try {
	            proyectoService.eliminar(id);
	            redirectAttrs.addFlashAttribute("mensaje", "Proyecto eliminado exitosamente");
	        } catch (IllegalStateException exception) {
	            redirectAttrs.addFlashAttribute("error", exception.getMessage());
	        }
	        return "redirect:/admin/proyectos";
	    }
}
