package com.construplan.campo.controller;
import java.math.BigDecimal;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.construplan.campo.model.entity.Meta;
import com.construplan.campo.model.entity.Tarea;
import com.construplan.campo.service.MetaService;
import com.construplan.campo.service.TareaService;

@Controller
@RequestMapping("/campo/metas")
public class MetaController {

    @Autowired
    private MetaService metaService;

    @Autowired
    private TareaService tareaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("metas", metaService.listarTodas());
        return "campo/metas";
    }

    @GetMapping("/nueva")
    public String formularioCrear(@RequestParam(required = false) Integer idTarea, Model model) {
        Tarea tareaSeleccionada = null;
        if (idTarea != null)
            tareaSeleccionada = tareaService.obtenerPorId(idTarea);

        model.addAttribute("meta", new Meta());
        model.addAttribute("tareas", tareaService.listarTodas());
        model.addAttribute("tareaSeleccionada", tareaSeleccionada);
        model.addAttribute("action", "create");
        return "campo/meta-form";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable int id, Model model,
                                   RedirectAttributes redirectAttrs) {
        try {
            Meta meta = metaService.obtenerPorId(id);
            model.addAttribute("meta", meta);
            model.addAttribute("tareas", tareaService.listarTodas());
            model.addAttribute("tareaSeleccionada", meta.getTarea());
            model.addAttribute("action", "update");
            return "campo/meta-form";
        } catch (IllegalStateException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/campo/metas";
        }
    }

    @PostMapping
    public String crear(@RequestParam int idTarea,
                        @RequestParam BigDecimal cantidad,
                        @RequestParam BigDecimal horasEquivalentes,
                        @RequestParam(defaultValue = "false") boolean esLibre,
                        Model model, RedirectAttributes redirectAttrs) {
        try {
            Tarea tarea = tareaService.obtenerPorId(idTarea);
            Meta meta = Meta.builder()
                    .tarea(tarea)
                    .cantidad(cantidad)
                    .horasEquivalentes(horasEquivalentes)
                    .libre(esLibre)
                    .build();
            metaService.registrarMeta(meta);
            redirectAttrs.addFlashAttribute("mensaje", "Meta creada exitosamente");
            return "redirect:/campo/metas";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tareas", tareaService.listarTodas());
            model.addAttribute("meta", new Meta());
            model.addAttribute("action", "create");
            return "campo/meta-form";
        }
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable int id,
                             @RequestParam int idTarea,
                             @RequestParam BigDecimal cantidad,
                             @RequestParam BigDecimal horasEquivalentes,
                             @RequestParam(defaultValue = "false") boolean esLibre,
                             Model model, RedirectAttributes redirectAttrs) {
        try {
            Tarea tarea = tareaService.obtenerPorId(idTarea);
            Meta meta = metaService.obtenerPorId(id);
            meta.setTarea(tarea);
            meta.setCantidad(cantidad);
            meta.setHorasEquivalentes(horasEquivalentes);
            meta.setLibre(esLibre);
            metaService.actualizarMeta(meta);
            redirectAttrs.addFlashAttribute("mensaje", "Meta actualizada exitosamente");
            return "redirect:/campo/metas";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tareas", tareaService.listarTodas());
            model.addAttribute("action", "update");
            return "campo/meta-form";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable int id, RedirectAttributes redirectAttrs) {
        try {
            metaService.eliminarMeta(id);
            redirectAttrs.addFlashAttribute("mensaje", "Meta eliminada exitosamente");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/campo/metas";
    }
}