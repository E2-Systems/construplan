package com.construplan.oficina.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.construplan.oficina.model.dto.WeeklySummaryDTO;
import com.construplan.oficina.model.entity.Planilla;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * Servicio encargado de la orquestación y generación de reportes en formato PDF 
 * utilizando la plantilla JasperReports de planillas semanales.
 */
@Service
public class ReportService {

    @Autowired
    private PlanillaService planillaService;

    /**
     * Recupera los datos de planillas semanales y compila la plantilla JRXML en caliente 
     * para exportar el reporte final en formato PDF.
     *
     * @param startOfWeek Fecha de inicio de la semana de interés (Lunes).
     * @return Arreglo de bytes del archivo PDF generado.
     */
    public byte[] generateWeeklyPayrollReportPdf(LocalDate startOfWeek) {
        // Guard temprano para validar el parámetro de entrada
        if (startOfWeek == null) {
            throw new IllegalArgumentException("La fecha de inicio de semana no puede ser nula");
        }

        List<Planilla> payrolls = planillaService.getPayrollsByStartDate(startOfWeek);
        // Guard temprano: si no hay planillas para esa fecha, lanzar excepción descriptiva
        if (payrolls.isEmpty()) {
            throw new IllegalStateException("No existen planillas generadas para la semana seleccionada.");
        }

        WeeklySummaryDTO summary = planillaService.getWeeklySummary(startOfWeek);
        LocalDate endOfWeek = startOfWeek.plusDays(5); // Rango de lunes a sábado
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Configuración de los parámetros del reporte Jasper
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("fechaInicio", startOfWeek.format(formatter));
        parameters.put("fechaFin", endOfWeek.format(formatter));
        parameters.put("totalPlanillas", summary.getTotalPayrolls());
        parameters.put("planillasPagadas", summary.getPaidPayrolls());
        parameters.put("planillasPendientes", summary.getPendingPayrolls());
        parameters.put("totalHorasBase", summary.getTotalBaseHours() != null ? summary.getTotalBaseHours() : BigDecimal.ZERO);
        parameters.put("totalHorasExtra", summary.getTotalExtraHours() != null ? summary.getTotalExtraHours() : BigDecimal.ZERO);
        parameters.put("totalPago", summary.getTotalPayment() != null ? summary.getTotalPayment() : BigDecimal.ZERO);

        // Mapeo estructurado a DTO plano compatible con JasperBeans
        List<WeeklyPayrollReportDetail> details = payrolls.stream()
                .map(p -> new WeeklyPayrollReportDetail(
                        p.getIdPlanilla(),
                        p.getEmpleado().getNombreCompleto(),
                        p.getEmpleado().getCategoria() != null ? p.getEmpleado().getCategoria().name() : "No asignada",
                        p.getTotalHorasBase(),
                        p.getTotalHorasExtra(),
                        p.getTotalPago(),
                        p.getEstado().name()
                ))
                .collect(Collectors.toList());

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(details);

        try (InputStream reportStream = getClass().getResourceAsStream("/reports/weekly_payroll.jrxml")) {
            // Guard temprano: si el archivo jrxml no se localiza en el classpath
            if (reportStream == null) {
                throw new IllegalStateException("No se encontró la plantilla de reporte jrxml en resources/reports.");
            }

            // Compilación y rellenado en tiempo de ejecución del reporte
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (Exception e) {
            throw new RuntimeException("Error crítico al generar el reporte de planillas: " + e.getMessage(), e);
        }
    }

    /**
     * Estructura DTO interna y estática requerida por el Jasper Bean DataSource.
     * Posee getters públicos para que la reflexión interna de JasperReports lea los campos.
     */
    public static class WeeklyPayrollReportDetail {
        private final Integer idPlanilla;
        private final String nombreEmpleado;
        private final String categoria;
        private final BigDecimal horasBase;
        private final BigDecimal horasExtra;
        private final BigDecimal totalPago;
        private final String estado;

        public WeeklyPayrollReportDetail(Integer idPlanilla, String nombreEmpleado, String categoria,
                                          BigDecimal horasBase, BigDecimal horasExtra, BigDecimal totalPago, String estado) {
            this.idPlanilla = idPlanilla;
            this.nombreEmpleado = nombreEmpleado;
            this.categoria = categoria;
            this.horasBase = horasBase;
            this.horasExtra = horasExtra;
            this.totalPago = totalPago;
            this.estado = estado;
        }

        public Integer getIdPlanilla() {
            return idPlanilla;
        }

        public String getNombreEmpleado() {
            return nombreEmpleado;
        }

        public String getCategoria() {
            return categoria;
        }

        public BigDecimal getHorasBase() {
            return horasBase;
        }

        public BigDecimal getHorasExtra() {
            return horasExtra;
        }

        public BigDecimal getTotalPago() {
            return totalPago;
        }

        public String getEstado() {
            return estado;
        }
    }
}
