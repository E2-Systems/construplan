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

@Controller
@RequestMapping("/campo/proyectos")
public class ProyectoController {
	  @Autowired
	    private ProyectoService proyectoService;

	    @GetMapping
	    public String listar(Model model) {
	        model.addAttribute("proyectos", proyectoService.listarTodos());
	        return "campo/proyectos";
	    }

	    @GetMapping("/nuevo")
	    public String formularioCrear(Model model) {
	        model.addAttribute("proyecto", new Proyecto());
	        model.addAttribute("action", "create");
	        return "campo/proyecto-form";
	    }

	    @GetMapping("/{id}/editar")
	    public String formularioEditar(@PathVariable int id, Model model,
	                                   RedirectAttributes redirectAttrs) {
	        try {
	            model.addAttribute("proyecto", proyectoService.obtenerPorId(id));
	            model.addAttribute("action", "update");
	            return "campo/proyecto-form";
	        } catch (IllegalStateException e) {
	            redirectAttrs.addFlashAttribute("error", e.getMessage());
	            return "redirect:/campo/proyectos";
	        }
	    }

	    @PostMapping
	    public String crear(@ModelAttribute Proyecto proyecto, Model model,
	                        RedirectAttributes redirectAttrs) {
	        try {
	            proyectoService.guardar(proyecto);
	            redirectAttrs.addFlashAttribute("mensaje", "Proyecto creado exitosamente");
	            return "redirect:/campo/proyectos";
	        } catch (IllegalArgumentException | IllegalStateException e) {
	            model.addAttribute("error", e.getMessage());
	            model.addAttribute("proyecto", proyecto);
	            model.addAttribute("action", "create");
	            return "campo/proyecto-form";
	        }
	    }

	    @PostMapping("/{id}")
	    public String actualizar(@PathVariable int id,
	                             @ModelAttribute Proyecto proyecto,
	                             Model model, RedirectAttributes redirectAttrs) {
	        proyecto.setIdProyecto(id);
	        try {
	            proyectoService.actualizar(proyecto);
	            redirectAttrs.addFlashAttribute("mensaje", "Proyecto actualizado exitosamente");
	            return "redirect:/campo/proyectos";
	        } catch (IllegalArgumentException | IllegalStateException e) {
	            model.addAttribute("error", e.getMessage());
	            model.addAttribute("proyecto", proyecto);
	            model.addAttribute("action", "update");
	            return "campo/proyecto-form";
	        }
	    }

	    @PostMapping("/{id}/estado")
	    public String cambiarEstado(@PathVariable int id,
	                                @RequestParam boolean activo,
	                                RedirectAttributes redirectAttrs) {
	        try {
	            proyectoService.activarDesactivar(id, activo);
	            redirectAttrs.addFlashAttribute("mensaje",
	                    activo ? "Proyecto activado" : "Proyecto desactivado");
	        } catch (IllegalStateException e) {
	            redirectAttrs.addFlashAttribute("error", e.getMessage());
	        }
	        return "redirect:/campo/proyectos";
	    }

	    @PostMapping("/{id}/eliminar")
	    public String eliminar(@PathVariable int id, RedirectAttributes redirectAttrs) {
	        try {
	            proyectoService.eliminar(id);
	            redirectAttrs.addFlashAttribute("mensaje", "Proyecto eliminado exitosamente");
	        } catch (IllegalStateException e) {
	            redirectAttrs.addFlashAttribute("error", e.getMessage());
	        }
	        return "redirect:/campo/proyectos";
	    }
}
