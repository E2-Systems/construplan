package com.construplan.campo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.construplan.admin.service.ProyectoService;
import com.construplan.campo.model.entity.AsignacionTarea;
import com.construplan.campo.model.entity.Modalidad;
import com.construplan.campo.service.AsignacionTareaService;
import com.construplan.campo.service.MetaService;
import com.construplan.service.EmpleadoService;



@Controller
@RequestMapping("/campo/asignaciones")
public class AsignacionTareaController {
	 @Autowired
	    private AsignacionTareaService asignacionService;

	    @Autowired
	    private EmpleadoService empleadoService;

	    @Autowired
	    private MetaService metaService;

	    @Autowired
	    private ProyectoService proyectoService;

	    // ── GET /campo/asignaciones ───────────────────────────────────────────────
	    @GetMapping
	    public String listar(@RequestParam(required = false)
	                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
	                         Authentication authentication,
	                         Model model) {

	        List<AsignacionTarea> asignaciones = fecha != null
	                ? asignacionService.listarPorFecha(fecha)
	                : asignacionService.listarTodas();

	        model.addAttribute("asignaciones", asignaciones);
	        model.addAttribute("fechaFiltro",  fecha);
	        model.addAttribute("username",     authentication.getName());
	        return "campo/asignaciones";
	    }

	    // ── GET /campo/asignaciones/nueva ─────────────────────────────────────────
	    @GetMapping("/nueva")
	    public String formularioCrear(Model model) {
	        model.addAttribute("empleados",  empleadoService.listarActivos());
	        model.addAttribute("metas",      metaService.listarTodas());
	        model.addAttribute("proyectos",  proyectoService.listarTodos());
	        model.addAttribute("modalidades", Modalidad.values());
	        model.addAttribute("action",     "create");
	        return "campo/asignacion-form";
	    }

	    // ── GET /campo/asignaciones/{id}/editar ───────────────────────────────────
	    @GetMapping("/{id}/editar")
	    public String formularioEditar(@PathVariable int id, Model model,
	                                   RedirectAttributes redirectAttrs) {
	        try {
	            AsignacionTarea asignacion = asignacionService.obtenerPorId(id);
	            model.addAttribute("asignacion", asignacion);
	            model.addAttribute("empleados",  empleadoService.listarActivos());
	            model.addAttribute("metas",      metaService.listarTodas());
	            model.addAttribute("proyectos",  proyectoService.listarTodos());
	            model.addAttribute("modalidades", Modalidad.values());
	            model.addAttribute("action",     "update");
	            return "campo/asignacion-form";
	        } catch (IllegalStateException e) {
	            redirectAttrs.addFlashAttribute("error", e.getMessage());
	            return "redirect:/campo/asignaciones";
	        }
	    }

	    // ── GET /campo/asignaciones/{id} ──────────────────────────────────────────
	    @GetMapping("/{id}")
	    public String detalle(@PathVariable int id, Model model,
	                          RedirectAttributes redirectAttrs) {
	        try {
	            model.addAttribute("asignacion", asignacionService.obtenerPorId(id));
	            return "campo/asignacion-detalle";
	        } catch (IllegalStateException e) {
	            redirectAttrs.addFlashAttribute("error", e.getMessage());
	            return "redirect:/campo/asignaciones";
	        }
	    }

	    // ── POST /campo/asignaciones ──────────────────────────────────────────────
	    @PostMapping
	    public String crear(@RequestParam int idEmpleado,
	                        @RequestParam int idMeta,
	                        @RequestParam int idProyecto,
	                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
	                        @RequestParam Modalidad modalidad,
	                        Model model, RedirectAttributes redirectAttrs) {
	        try {
	            asignacionService.crearAsignacion(idEmpleado, idMeta, idProyecto, fecha, modalidad);
	            redirectAttrs.addFlashAttribute("mensaje", "Asignación creada exitosamente");
	            return "redirect:/campo/asignaciones";
	        } catch (IllegalArgumentException | IllegalStateException e) {
	            model.addAttribute("error",      e.getMessage());
	            model.addAttribute("empleados",  empleadoService.listarActivos());
	            model.addAttribute("metas",      metaService.listarTodas());
	            model.addAttribute("proyectos",  proyectoService.listarTodos());
	            model.addAttribute("modalidades", Modalidad.values());
	            model.addAttribute("action",     "create");
	            return "campo/asignacion-form";
	        }
	    }

	    // ── POST /campo/asignaciones/{id} ─────────────────────────────────────────
	    @PostMapping("/{id}")
	    public String actualizar(@PathVariable int id,
	                             @RequestParam int idMeta,
	                             @RequestParam int idProyecto,
	                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
	                             @RequestParam Modalidad modalidad,
	                             Model model, RedirectAttributes redirectAttrs) {
	        try {
	            asignacionService.actualizarAsignacion(id, idMeta, idProyecto, fecha, modalidad);
	            redirectAttrs.addFlashAttribute("mensaje", "Asignación actualizada exitosamente");
	            return "redirect:/campo/asignaciones";
	        } catch (IllegalArgumentException | IllegalStateException e) {
	            model.addAttribute("error",      e.getMessage());
	            model.addAttribute("empleados",  empleadoService.listarActivos());
	            model.addAttribute("metas",      metaService.listarTodas());
	            model.addAttribute("proyectos",  proyectoService.listarTodos());
	            model.addAttribute("modalidades", Modalidad.values());
	            model.addAttribute("action",     "update");
	            return "campo/asignacion-form";
	        }
	    }

	    // ── POST /campo/asignaciones/{id}/eliminar ────────────────────────────────
	    @PostMapping("/{id}/eliminar")
	    public String eliminar(@PathVariable int id, RedirectAttributes redirectAttrs) {
	        try {
	            asignacionService.eliminarAsignacion(id);
	            redirectAttrs.addFlashAttribute("mensaje", "Asignación eliminada exitosamente");
	        } catch (IllegalStateException e) {
	            redirectAttrs.addFlashAttribute("error", e.getMessage());
	        }
	        return "redirect:/campo/asignaciones";
	    }

	    // ── POST /campo/asignaciones/{id}/finalizar ───────────────────────────────
	    @PostMapping("/{id}/finalizar")
	    public String finalizar(@PathVariable int id, RedirectAttributes redirectAttrs) {
	        try {
	            asignacionService.finalizarTarea(id);
	            redirectAttrs.addFlashAttribute("mensaje", "Tarea finalizada exitosamente");
	        } catch (IllegalStateException e) {
	            redirectAttrs.addFlashAttribute("error", e.getMessage());
	        }
	        return "redirect:/campo/asignaciones";
	    }
}
