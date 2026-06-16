package com.construplan.oficina.controller;


import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.construplan.empleado.model.entity.Categoria;
import com.construplan.empleado.model.entity.Empleado;
import com.construplan.empleado.service.EmpleadoService;
import com.construplan.oficina.model.entity.EstadoPlanilla;
import com.construplan.oficina.model.entity.PeriodoPago;
import com.construplan.oficina.model.entity.Sueldo;
import com.construplan.oficina.repository.AjustePlanillaRepository;
import com.construplan.oficina.repository.PlanillaRepository;
import com.construplan.oficina.service.SueldoService;

// La verificación de sesión y rol la gestiona Spring Security en SecurityConfig (/oficina/** → ROLE_OFICINA)
@Controller
@RequestMapping("/oficina")
public class OficinaController {

	 @Autowired
	    private EmpleadoService empleadoService;

	    @Autowired
	    private SueldoService sueldoService;
	    
	    @Autowired
	    private PlanillaRepository planillaRepository;

	    @Autowired
	    private AjustePlanillaRepository ajustePlanillaRepository;
	    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        // Fecha actual formateada en español-Perú
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "EEEE, dd 'de' MMMM 'de' yyyy", new Locale("es", "PE"));
        String fechaActual = hoy.format(formatter);

        // Hora actual formateada para mostrar en el encabezado
        String horaActual = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

     // Datos estadísticos calculados dinámicamente
        long planillasGeneradas = planillaRepository.count();
        long planillasPendientes = planillaRepository.countByEstado(EstadoPlanilla.GENERADA);
        int empleadosActivos = empleadoService.listarActivos().size();
        BigDecimal totalPorPagarBigDecimal = planillaRepository.sumTotalPagoByEstado(EstadoPlanilla.GENERADA);
        double totalPorPagar = totalPorPagarBigDecimal != null ? totalPorPagarBigDecimal.doubleValue() : 0.0;

        // Rango de la semana laboral actual (lunes a sábado)
        LocalDate inicioSemana = hoy.with(DayOfWeek.MONDAY);
        LocalDate finSemana = inicioSemana.plusDays(5);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String semanaActual = inicioSemana.format(dateFormatter) + " - " + finSemana.format(dateFormatter);

     // Contadores de alertas pendientes reales
        long ajustesPendientes = ajustePlanillaRepository.countByPlanilla_Estado(EstadoPlanilla.GENERADA);
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
    @GetMapping("/empleados")
    public String listEmployees(Model model) {
        List<Empleado> employees = empleadoService.listarTodos();
        Map<Integer, Sueldo> activeSalaries = new HashMap<>();

        for (Empleado employee : employees) {
            Optional<Sueldo> activeSalaryOpt = sueldoService.obtenerSueldoActivo(employee.getIdEmpleado());
            activeSalaryOpt.ifPresent(sueldo -> activeSalaries.put(employee.getIdEmpleado(), sueldo));
        }

        model.addAttribute("empleados", employees);
        model.addAttribute("sueldosActivos", activeSalaries);
        return "oficina/empleados/lista";
    }

    @GetMapping("/empleados/gestionar/{id}")
    public String manageEmployee(@PathVariable("id") int idEmployee, Model model) {
        Empleado employee = empleadoService.obtenerPorId(idEmployee);
        List<Sueldo> salaryHistory = sueldoService.obtenerHistorialSueldos(idEmployee);
        Optional<Sueldo> activeSalaryOpt = sueldoService.obtenerSueldoActivo(idEmployee);

        model.addAttribute("empleado", employee);
        model.addAttribute("historialSueldos", salaryHistory);
        model.addAttribute("sueldoActivo", activeSalaryOpt.orElse(null));
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("periodos", PeriodoPago.values());
        return "oficina/empleados/gestionar";
    }

    @PostMapping("/empleados/guardar-gestion")
    public String saveManagement(@RequestParam("idEmpleado") int idEmployee,
                                 @RequestParam("categoria") Categoria category,
                                 @RequestParam("sueldo") BigDecimal salary,
                                 @RequestParam("periodo") PeriodoPago period,
                                 @RequestParam("fechaInicio") String startDateStr,
                                 RedirectAttributes redirectAttributes) {
        LocalDate startDate;
        try {
            startDate = LocalDate.parse(startDateStr);
        } catch (DateTimeParseException exception) {
            redirectAttributes.addFlashAttribute("error", "Formato de fecha inválido. Utilice AAAA-MM-DD.");
            return "redirect:/oficina/empleados/gestionar/" + idEmployee;
        }

        try {
            empleadoService.actualizarCategoriaYSueldo(idEmployee, category, salary, period, startDate);
            redirectAttributes.addFlashAttribute("mensaje", "¡Categoría y acuerdo salarial actualizados con éxito!");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar: " + exception.getMessage());
            return "redirect:/oficina/empleados/gestionar/" + idEmployee;
        }

        return "redirect:/oficina/empleados";
    }
    

    @PostMapping("/empleados/sueldos/eliminar/{id}")
    public String deleteSalaryAgreement(@PathVariable("id") int idSueldo, RedirectAttributes redirectAttributes) {
        try {
            int idEmployee = sueldoService.eliminarSueldo(idSueldo);
            redirectAttributes.addFlashAttribute("mensaje", "El acuerdo salarial ha sido eliminado de forma definitiva y el historial de vigencias ha sido recalculado.");
            return "redirect:/oficina/empleados/gestionar/" + idEmployee;
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("error", "Error al intentar eliminar el acuerdo salarial: " + exception.getMessage());
            return "redirect:/oficina/empleados";
        }
    }
}
