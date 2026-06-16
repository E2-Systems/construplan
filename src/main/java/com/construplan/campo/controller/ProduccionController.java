package com.construplan.campo.controller;


import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.construplan.campo.model.dto.EmpleadoRendimientoDTO;
import com.construplan.campo.model.dto.ProyectoAvanceDTO;
import com.construplan.campo.model.entity.AsignacionTarea;
import com.construplan.campo.repository.AsignacionTareaRepository;


/**
 * Controlador para la visualización del avance físico de obras y rendimiento
 * del personal en campo. Calcula las métricas agregadas por proyecto y operario
 * para la toma de decisiones del Ingeniero de Campo.
 */
@Controller
@RequestMapping("/campo/produccion")
public class ProduccionController {

    @Autowired
    private AsignacionTareaRepository asignacionTareaRepository;

    /**
     * Muestra el panel de indicadores de producción física de la semana.
     * Soporta filtrado dinámico de semanas para auditar el desempeño histórico.
     */
    @GetMapping
    public String showProductionDashboard(@RequestParam(name = "semana", required = false) String weekParam, Model model) {
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);

        if (weekParam != null && !weekParam.isBlank()) {
            try {
                startOfWeek = LocalDate.parse(weekParam);
            } catch (Exception ignored) {
                // Si el formato de fecha es inválido, se ignora y se procede con la semana actual.
            }
        }

        LocalDate endOfWeek = startOfWeek.plusDays(6); // Lunes a Domingo

        List<AsignacionTarea> weeklyAssignments = asignacionTareaRepository.findByFechaBetween(startOfWeek, endOfWeek);

        // Agrupación y cálculo del avance de metas físicas por cada Obra/Proyecto
        Map<Integer, List<AsignacionTarea>> assignmentsByProject = weeklyAssignments.stream()
                .collect(Collectors.groupingBy(a -> a.getProyecto().getIdProyecto()));

        List<ProyectoAvanceDTO> projectProgress = assignmentsByProject.entrySet().stream()
                .map(entry -> {
                    List<AsignacionTarea> list = entry.getValue();
                    String projectName = list.get(0).getProyecto().getNombre();
                    int total = list.size();
                    int completed = (int) list.stream().filter(a -> a.getHoraMetaCompletada() != null).count();
                    double percentage = total > 0 ? (completed * 100.0 / total) : 0.0;

                    return ProyectoAvanceDTO.builder()
                            .idProyecto(entry.getKey())
                            .nombreProyecto(projectName)
                            .totalAsignadas(total)
                            .totalCompletadas(completed)
                            .porcentajeAvance(Math.round(percentage * 100.0) / 100.0)
                            .build();
                })
                .sorted(Comparator.comparing(ProyectoAvanceDTO::getNombreProyecto))
                .collect(Collectors.toList());

        // Agrupación y cálculo del rendimiento físico individual de los Empleados
        Map<Integer, List<AsignacionTarea>> assignmentsByEmployee = weeklyAssignments.stream()
                .collect(Collectors.groupingBy(a -> a.getEmpleado().getIdEmpleado()));

        List<EmpleadoRendimientoDTO> employeeEfficiency = assignmentsByEmployee.entrySet().stream()
                .map(entry -> {
                    List<AsignacionTarea> list = entry.getValue();
                    String fullName = list.get(0).getEmpleado().getNombreCompleto();
                    int total = list.size();
                    int completed = (int) list.stream().filter(a -> a.getHoraMetaCompletada() != null).count();
                    double percentage = total > 0 ? (completed * 100.0 / total) : 0.0;

                    return EmpleadoRendimientoDTO.builder()
                            .nombreCompleto(fullName)
                            .tareasAsignadas(total)
                            .tareasCompletadas(completed)
                            .porcentajeRendimiento(Math.round(percentage * 100.0) / 100.0)
                            .build();
                })
                .sorted(Comparator.comparing(EmpleadoRendimientoDTO::getPorcentajeRendimiento).reversed())
                .collect(Collectors.toList());

        // Cálculo de métricas acumulativas de la semana
        int totalWeeklyTasks = weeklyAssignments.size();
        int completedWeeklyTasks = (int) weeklyAssignments.stream()
                .filter(a -> a.getHoraMetaCompletada() != null)
                .count();

        double globalEfficiency = totalWeeklyTasks > 0 
                ? (completedWeeklyTasks * 100.0 / totalWeeklyTasks) 
                : 0.0;

        BigDecimal totalPhysicalHours = weeklyAssignments.stream()
                .filter(a -> a.getHoraMetaCompletada() != null)
                .map(a -> a.getMeta().getHorasEquivalentes())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String periodText = startOfWeek.format(dateFormatter) + " al " + endOfWeek.format(dateFormatter);

        model.addAttribute("semanaSeleccionada", startOfWeek);
        model.addAttribute("periodoTexto", periodText);
        model.addAttribute("asignaciones", weeklyAssignments);
        model.addAttribute("avancesProyectos", projectProgress);
        model.addAttribute("rendimientosEmpleados", employeeEfficiency);
        model.addAttribute("totalTareas", totalWeeklyTasks);
        model.addAttribute("tareasCompletadas", completedWeeklyTasks);
        model.addAttribute("eficienciaGlobal", Math.round(globalEfficiency * 100.0) / 100.0);
        model.addAttribute("horasFisicas", totalPhysicalHours);
        model.addAttribute("active", "produccion");

        return "campo/produccion";
    }
}
