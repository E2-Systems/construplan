package com.construplan.oficina.controller;


import java.time.DayOfWeek;
import java.time.LocalDate;
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

import com.construplan.empleado.service.EmpleadoService;
import com.construplan.oficina.model.dto.WeeklySummaryDTO;
import com.construplan.oficina.model.entity.EstadoPlanilla;
import com.construplan.oficina.model.entity.Planilla;
import com.construplan.oficina.service.PlanillaService;



@Controller
@RequestMapping("/oficina/planillas")
public class PlanillaController {

    @Autowired
    private PlanillaService planillaService;

    @Autowired
    private EmpleadoService empleadoService;

    /**
     * Lista todas las planillas generadas en el sistema.
     * Soporta filtrado opcional por el estado actual de la planilla (GENERADA o PAGADA).
     */
    @GetMapping
    public String listPayrolls(@RequestParam(name = "estado", required = false) EstadoPlanilla status, Model model) {
        List<Planilla> payrolls = status != null ? 
                planillaService.getPayrollsByStatus(status) : 
                planillaService.getAllPayrolls();

        model.addAttribute("planillas", payrolls);
        model.addAttribute("estadoFiltro", status);
        model.addAttribute("estados", EstadoPlanilla.values());
        return "oficina/planillas/lista";
    }

    /**
     * Muestra el formulario para seleccionar la fecha de inicio y realizar la generación masiva de planillas.
     */
    @GetMapping("/generar")
    public String showGenerateForm(Model model) {
        // Sugerir por defecto el lunes de la semana actual para facilitar la experiencia del usuario de oficina técnica
        LocalDate suggestedStart = LocalDate.now().with(DayOfWeek.MONDAY);
        model.addAttribute("fechaSugerida", suggestedStart);
        return "oficina/planillas/generar";
    }

    /**
     * Procesa la generación masiva de planillas para todos los empleados activos con registros diarios aprobados.
     */
    @PostMapping("/generar")
    public String processGeneration(@RequestParam("fechaInicio") String startDateStr, RedirectAttributes redirectAttributes) {
        LocalDate startOfWeek;
        try {
            startOfWeek = LocalDate.parse(startDateStr);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "El formato de fecha de inicio de semana es inválido.");
            return "redirect:/oficina/planillas/generar";
        }

        // Validar que la fecha seleccionada sea lunes para mantener consistencia con el periodo de lunes a sábado
        if (startOfWeek.getDayOfWeek() != DayOfWeek.MONDAY) {
            redirectAttributes.addFlashAttribute("error", "La fecha de inicio de planilla semanal debe ser un día Lunes.");
            return "redirect:/oficina/planillas/generar";
        }

        try {
            List<Planilla> generated = planillaService.generatePayrollForActiveEmployees(startOfWeek);
            
            // Guard temprano: Informar apropiadamente si no había datos pendientes para procesar
            if (generated.isEmpty()) {
                redirectAttributes.addFlashAttribute("info", "No se encontraron empleados activos con registros aprobados pendientes para generar planilla en el rango indicado, o las planillas ya fueron generadas previamente.");
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "Se generaron exitosamente " + generated.size() + " planillas para la semana seleccionada.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado durante el procesamiento: " + e.getMessage());
            return "redirect:/oficina/planillas/generar";
        }

        return "redirect:/oficina/planillas";
    }

    /**
     * Muestra el detalle pormenorizado de una planilla seleccionada, incluyendo el desglose diario y los ajustes.
     */
    @GetMapping("/detalle/{id}")
    public String viewDetail(@PathVariable("id") int idPlanilla, Model model, RedirectAttributes redirectAttributes) {
        Planilla payroll = planillaService.getPayroll(idPlanilla)
                .orElse(null);

        // Guard temprano: redireccionar si la planilla buscada no existe en base de datos
        if (payroll == null) {
            redirectAttributes.addFlashAttribute("error", "La planilla solicitada no existe.");
            return "redirect:/oficina/planillas";
        }

        model.addAttribute("planilla", payroll);
        return "oficina/planillas/detalle";
    }

    /**
     * Cambia el estado de la planilla a PAGADA, cerrando administrativamente el periodo de cobro para el empleado.
     */
    @PostMapping("/detalle/{id}/pagar")
    public String markAsPaid(@PathVariable("id") int idPlanilla, RedirectAttributes redirectAttributes) {
        try {
            planillaService.markAsPaid(idPlanilla);
            redirectAttributes.addFlashAttribute("mensaje", "La planilla ha sido marcada como PAGADA con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar el pago: " + e.getMessage());
        }
        return "redirect:/oficina/planillas/detalle/" + idPlanilla;
    }
    

    /**
     * Elimina una planilla de forma permanente si el estado lo permite.
     */
    @PostMapping("/detalle/{id}/eliminar")
    public String deletePayroll(@PathVariable("id") int idPlanilla, RedirectAttributes redirectAttributes) {
        try {
            planillaService.deletePayroll(idPlanilla);
            redirectAttributes.addFlashAttribute("mensaje", "La planilla ha sido eliminada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al intentar eliminar la planilla: " + e.getMessage());
            return "redirect:/oficina/planillas/detalle/" + idPlanilla;
        }
        return "redirect:/oficina/planillas";
    }
    /**
     * Muestra el resumen general de pagos de la semana seleccionada,
     * agregando las métricas de todas las planillas individuales generadas en ese rango.
     */
    @GetMapping("/resumen-semanal")
    public String weeklySummary(@RequestParam(name = "semana", required = false) String weekParam, Model model) {
        List<java.time.LocalDate> availableWeeks = planillaService.getUniqueStartDates();

        // Por defecto seleccionar la semana más reciente disponible en el sistema
        java.time.LocalDate selectedWeek = null;
        if (weekParam != null && !weekParam.isEmpty()) {
            try {
                selectedWeek = java.time.LocalDate.parse(weekParam);
            } catch (Exception ignored) {
                // Formato inválido, se ignora y se usa la semana más reciente
            }
        }

        // Guard temprano: si no hay semanas disponibles, mostrar la vista vacía
        if (availableWeeks.isEmpty()) {
            model.addAttribute("semanasDisponibles", availableWeeks);
            model.addAttribute("hasDatos", false);
            return "oficina/planillas/resumen-semanal";
        }

        if (selectedWeek == null) {
            selectedWeek = availableWeeks.get(0);
        }

        java.time.LocalDate endOfWeek = selectedWeek.plusDays(5);

        // Obtener las planillas individuales de la semana seleccionada
        List<Planilla> weeklyPayrolls = planillaService.getPayrollsByStartDate(selectedWeek);

        // Métricas agregadas globales para la semana utilizando el DTO
        WeeklySummaryDTO summary = planillaService.getWeeklySummary(selectedWeek);
        

        model.addAttribute("semanasDisponibles", availableWeeks);
        model.addAttribute("semanaSeleccionada", selectedWeek);
        model.addAttribute("fechaFinSemana", endOfWeek);
        model.addAttribute("planillasSemana", weeklyPayrolls);
        model.addAttribute("summary", summary);
        model.addAttribute("hasDatos", true);

        return "oficina/planillas/resumen-semanal";
    }
}

