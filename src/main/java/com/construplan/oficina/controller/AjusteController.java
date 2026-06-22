package com.construplan.oficina.controller;


import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.construplan.oficina.model.entity.AjustePlanilla;
import com.construplan.oficina.model.entity.Planilla;
import com.construplan.oficina.model.entity.TipoAjuste;
import com.construplan.oficina.repository.AjustePlanillaRepository;
import com.construplan.oficina.service.PlanillaService;

@Controller
@RequestMapping("/oficina")
public class AjusteController {

    @Autowired
    private PlanillaService planillaService;

    @Autowired
    private AjustePlanillaRepository ajustePlanillaRepository;

    /**
     * Muestra la lista general de todos los ajustes extraordinarios registrados en el sistema.
     */
    @GetMapping("/ajustes")
    public String listAllAdjustments(Model model) {
        List<AjustePlanilla> adjustments = ajustePlanillaRepository.findAll();
        model.addAttribute("ajustes", adjustments);
        return "oficina/ajustes/lista";
    }

    /**
     * Muestra el formulario de registro de un nuevo ajuste (adelanto o descuento) para una planilla específica.
     */
    @GetMapping("/planillas/{id}/ajustes/nuevo")
    public String showAdjustmentForm(@PathVariable("id") int idPlanilla, Model model, RedirectAttributes redirectAttributes) {
        Planilla payroll = planillaService.getPayroll(idPlanilla).orElse(null);

        // Guard temprano: evitar renderizar formulario si la planilla asociada no existe
        if (payroll == null) {
            redirectAttributes.addFlashAttribute("error", "La planilla indicada no existe.");
            return "redirect:/oficina/planillas";
        }

        // Guard temprano: no se permite registrar adelantos/descuentos sobre una planilla ya cerrada
        if (payroll.getEstado() == com.construplan.oficina.model.entity.EstadoPlanilla.PAGADA) {
            redirectAttributes.addFlashAttribute("error", "No se pueden agregar ajustes a una planilla que ya ha sido pagada.");
            return "redirect:/oficina/planillas/detalle/" + idPlanilla;
        }

        model.addAttribute("planilla", payroll);
        model.addAttribute("tipos", TipoAjuste.values());
        return "oficina/planillas/ajuste-form";
    }

    /**
     * Procesa la inserción del nuevo ajuste extraordinario y realiza el recalculo automático del pago neto.
     */
    @PostMapping("/planillas/{id}/ajustes/guardar")
    public String saveAdjustment(@PathVariable("id") int idPlanilla,
                                 @RequestParam("tipo") TipoAjuste type,
                                 @RequestParam("monto") BigDecimal amount,
                                 @RequestParam("motivo") String reason,
                                 RedirectAttributes redirectAttributes) {
        try {
            planillaService.addAdjustment(idPlanilla, type, amount, reason);
            redirectAttributes.addFlashAttribute("mensaje", "Ajuste extraordinario registrado y aplicado correctamente.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/oficina/planillas/" + idPlanilla + "/ajustes/nuevo";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado al intentar guardar el ajuste.");
            return "redirect:/oficina/planillas/" + idPlanilla + "/ajustes/nuevo";
        }

        return "redirect:/oficina/planillas/detalle/" + idPlanilla;
    }

    /**
     * Elimina un ajuste y recalcula el monto neto de la planilla asociada.
     */
    @PostMapping("/ajustes/{id}/eliminar")
    public String deleteAdjustment(@PathVariable("id") int idAjuste, RedirectAttributes redirectAttributes) {
        int idPlanilla = 0;
        try {
            AjustePlanilla adjustment = ajustePlanillaRepository.findById(idAjuste).orElse(null);
            if (adjustment != null) {
                idPlanilla = adjustment.getPlanilla().getIdPlanilla();
            }
            planillaService.deleteAdjustment(idAjuste);
            redirectAttributes.addFlashAttribute("mensaje", "El ajuste ha sido eliminado y el total de la planilla se recalculó.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el ajuste: " + e.getMessage());
        }

        // Si logramos recuperar la planilla, redireccionamos a su detalle; si no, a la lista general
        if (idPlanilla > 0) {
            return "redirect:/oficina/planillas/detalle/" + idPlanilla;
        }
        return "redirect:/oficina/planillas";
    }
}
