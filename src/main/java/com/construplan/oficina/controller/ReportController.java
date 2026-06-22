package com.construplan.oficina.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.construplan.oficina.service.PlanillaService;
import com.construplan.oficina.service.ReportService;



/**
 * Controlador web responsable de gestionar los endpoints de consulta y 
 * descarga de los reportes PDF de la oficina técnica.
 */
@Controller
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private PlanillaService planillaService;

    /**
     * Mapeo de redirección inteligente para el enlace general /reportes de la barra de navegación.
     * Enruta a la sección específica del rol del usuario.
     *
     * @param authentication Información de autenticación del usuario.
     * @return Ruta de redirección según el rol de seguridad.
     */
    @GetMapping("/reportes")
    public String redirectGeneralReportRoute(Authentication authentication) {
        // Guard temprano: si no está autenticado, enrutar a la pantalla de login.
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        String authority = authentication.getAuthorities().iterator().next().getAuthority();
        if ("ROLE_ADMIN".equals(authority)) {
            return "redirect:/admin/reportes";
        }
        if ("ROLE_OFICINA".equals(authority)) {
            return "redirect:/oficina/reportes";
        }
        
        return "redirect:/dashboard";
    }

    /**
     * Carga el panel principal de descargas de reportes para el Ingeniero de Oficina,
     * inyectando las semanas disponibles de planillas en la base de datos.
     *
     * @param model Modelo Thymeleaf para inyección de datos a la vista.
     * @return Ruta de la plantilla HTML de reportes.
     */
    @GetMapping("/oficina/reportes")
    public String showReportsPanel(Model model) {
        List<LocalDate> uniqueStartDates = planillaService.getUniqueStartDates();
        model.addAttribute("semanasDisponibles", uniqueStartDates);
        return "oficina/reportes";
    }

    /**
     * Genera y sirve el archivo PDF correspondiente al reporte consolidado de planillas 
     * de una semana específica.
     *
     * @param fechaInicio Fecha de inicio de la semana a consultar (ISO format YYYY-MM-DD).
     * @param redirectAttrs Atributos de redirección flash en caso de excepciones.
     * @return Respuesta HTTP con el flujo binario del PDF.
     */
    @GetMapping("/oficina/reportes/descargar")
    public ResponseEntity<byte[]> downloadWeeklyPayrollReportPdf(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            RedirectAttributes redirectAttrs) {
        try {
            byte[] pdfBytes = reportService.generateWeeklyPayrollReportPdf(fechaInicio);
            String filename = "reporte-semanal-" + fechaInicio.toString() + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            // Guard temprano para manejo de excepciones redirigiendo al panel de reportes con feedback
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/oficina/reportes")
                    .build();
        }
    }
}
