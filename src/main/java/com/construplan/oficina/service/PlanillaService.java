package com.construplan.oficina.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.construplan.admin.service.SystemConfigurationService;
import com.construplan.empleado.model.entity.Empleado;
import com.construplan.empleado.model.entity.RegistroDiario;
import com.construplan.empleado.repository.EmpleadoRepository;
import com.construplan.empleado.repository.RegistroDiarioRepository;
import com.construplan.oficina.model.dto.WeeklySummaryDTO;
import com.construplan.oficina.model.entity.AjustePlanilla;
import com.construplan.oficina.model.entity.EstadoPlanilla;
import com.construplan.oficina.model.entity.PeriodoPago;
import com.construplan.oficina.model.entity.Planilla;
import com.construplan.oficina.model.entity.PlanillaDetalle;
import com.construplan.oficina.model.entity.Sueldo;
import com.construplan.oficina.model.entity.TipoAjuste;
import com.construplan.oficina.repository.AjustePlanillaRepository;
import com.construplan.oficina.repository.PlanillaDetalleRepository;
import com.construplan.oficina.repository.PlanillaRepository;
import com.construplan.oficina.repository.SueldoRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PlanillaService {

    @Autowired
    private PlanillaRepository planillaRepository;

    @Autowired
    private PlanillaDetalleRepository planillaDetalleRepository;

    @Autowired
    private AjustePlanillaRepository ajustePlanillaRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private SueldoRepository sueldoRepository;

    @Autowired
    private RegistroDiarioRepository registroDiarioRepository;
    
    @Autowired
    private SystemConfigurationService systemConfigurationService;

    /**
     * Genera la planilla de un empleado para una semana determinada.
     * Calcula los montos en función de los registros diarios aprobados y la tarifa vigente.
     */
    @Transactional
    public Planilla generateWeeklyPayroll(int idEmpleado, LocalDate startOfWeek) {
        LocalDate endOfWeek = startOfWeek.plusDays(5); // Lunes a Sábado

        log.info("Iniciando generación de planilla para empleado {} desde {} hasta {}", 
                 idEmpleado, startOfWeek, endOfWeek);

        // Evitar duplicados
        if (planillaRepository.existsByEmpleado_IdEmpleadoAndFechaInicioAndFechaFin(idEmpleado, startOfWeek, endOfWeek)) {
            log.warn("Ya existe una planilla para empleado {} en el rango {} - {}", idEmpleado, startOfWeek, endOfWeek);
            throw new IllegalStateException("Ya existe una planilla generada para este empleado en el periodo seleccionado.");
        }

        Empleado employee = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> {
                    log.error("Empleado no encontrado con ID {}", idEmpleado);
                    return new IllegalArgumentException("Empleado no encontrado con ID: " + idEmpleado);
                });

        log.debug("Empleado encontrado: {}", employee);

        List<RegistroDiario> approvedRecords = registroDiarioRepository.findAprobadosByEmpleadoAndRango(idEmpleado, startOfWeek, endOfWeek);
        log.info("Registros aprobados encontrados: {}", approvedRecords.size());

        if (approvedRecords.isEmpty()) {
            log.warn("Empleado {} no tiene registros aprobados en el rango {} - {}", idEmpleado, startOfWeek, endOfWeek);
            throw new IllegalArgumentException("El empleado no cuenta con registros diarios de asistencia aprobados en el rango de fechas indicado.");
        }

        Planilla payroll = Planilla.builder()
                .empleado(employee)
                .fechaInicio(startOfWeek)
                .fechaFin(endOfWeek)
                .estado(EstadoPlanilla.GENERADA)
                .totalHorasBase(BigDecimal.ZERO)
                .totalHorasExtra(BigDecimal.ZERO)
                .totalPago(BigDecimal.ZERO)
                .detalles(new ArrayList<>())
                .ajustes(new ArrayList<>())
                .build();

        BigDecimal accumulatedBaseHours = BigDecimal.ZERO;
        BigDecimal accumulatedExtraHours = BigDecimal.ZERO;
        BigDecimal accumulatedPayment = BigDecimal.ZERO;

        for (RegistroDiario record : approvedRecords) {
            LocalDate recordDate = record.getAsignacion().getFecha();
            log.info("Procesando registro {} con fecha {}", record.getIdRegistro(), recordDate);

            Sueldo activeSalary = sueldoRepository.findSueldoActivoByEmpleadoAndFecha(idEmpleado, recordDate)
                    .orElseThrow(() -> {
                        log.info("Empleado {} no tiene sueldo activo para la fecha {}", idEmpleado, recordDate);
                        return new IllegalStateException("El empleado no cuenta con un sueldo configurado o activo para la fecha: " + recordDate);
                    });

            log.info("Sueldo activo encontrado: {}", activeSalary);

            BigDecimal dailyRate = calculateDailyRate(activeSalary); 
            // La jornada estándar de trabajo diario normal se lee dinámicamente de la configuración.
            BigDecimal hourlyRate = dailyRate.divide(systemConfigurationService.getJornadaEstandar(), 4, RoundingMode.HALF_UP);

            BigDecimal baseHours = record.getHorasBase() != null ? record.getHorasBase() : BigDecimal.ZERO;
            BigDecimal extraHours = record.getHorasExtra() != null ? record.getHorasExtra() : BigDecimal.ZERO;

            log.info("Horas base={}, horas extra={}", baseHours, extraHours);

            BigDecimal basePayment = baseHours.multiply(hourlyRate);         
            // El factor de recargo para el pago de horas extra se lee dinámicamente de la configuración.
            BigDecimal extraPayment = extraHours.multiply(hourlyRate).multiply(systemConfigurationService.getMultiplicadorHorasExtras());
            BigDecimal totalDailyPayment = basePayment.add(extraPayment);

            log.info("Pago diario calculado: {}", totalDailyPayment);

            PlanillaDetalle detail = PlanillaDetalle.builder()
                    .planilla(payroll)
                    .fecha(recordDate)
                    .horasBase(baseHours)
                    .horasExtra(extraHours)
                    .pagoDia(totalDailyPayment.setScale(2, RoundingMode.HALF_UP))
                    .build();

            payroll.getDetalles().add(detail);

            accumulatedBaseHours = accumulatedBaseHours.add(baseHours);
            accumulatedExtraHours = accumulatedExtraHours.add(extraHours);
            accumulatedPayment = accumulatedPayment.add(totalDailyPayment);
        }

        payroll.setTotalHorasBase(accumulatedBaseHours.setScale(2, RoundingMode.HALF_UP));
        payroll.setTotalHorasExtra(accumulatedExtraHours.setScale(2, RoundingMode.HALF_UP));
        payroll.setTotalPago(accumulatedPayment.setScale(2, RoundingMode.HALF_UP));

        log.info("Totales acumulados: horas base={}, horas extra={}, pago={}", 
                 payroll.getTotalHorasBase(), payroll.getTotalHorasExtra(), payroll.getTotalPago());

        Planilla savedPayroll = planillaRepository.save(payroll);
        log.info("Planilla guardada con ID {} para empleado {}", savedPayroll.getIdPlanilla(), idEmpleado);

        return savedPayroll;
    }

    /**
     * Genera la planilla de forma masiva para todos los empleados activos que cumplan con tener registros aprobados.
     */
    @Transactional
    public List<Planilla> generatePayrollForActiveEmployees(LocalDate startOfWeek) {
        LocalDate endOfWeek = startOfWeek.plusDays(5);
        List<Empleado> activeEmployees = empleadoRepository.findByActivoTrue();
        List<Planilla> generatedPayrolls = new ArrayList<>();
        
     

        for (Empleado employee : activeEmployees) {
        	   
               
            // Guard temprano: omitir si ya existe planilla generada para evitar errores en lote
            boolean exists = planillaRepository.existsByEmpleado_IdEmpleadoAndFechaInicioAndFechaFin(employee.getIdEmpleado(), startOfWeek, endOfWeek);
            if (exists) {       
                continue;
            }

            // Guard temprano: verificar si tiene registros aprobados en el rango antes de intentar la generación
            List<RegistroDiario> records = registroDiarioRepository.findAprobadosByEmpleadoAndRango(employee.getIdEmpleado(), startOfWeek, endOfWeek);
            if (records.isEmpty()) {
                continue;
            }

            try {
            	System.out.print("PAYROLLLL");
                Planilla payroll = generateWeeklyPayroll(employee.getIdEmpleado(), startOfWeek);
                System.out.print(payroll.toString());
                generatedPayrolls.add(payroll);
            } catch (Exception e) {
                // Capturar excepciones individuales para que una falla en un empleado no aborte la generación completa del lote
                // En un ambiente real, esto se loguearía para auditoría de oficina técnica
            }
        }

        return generatedPayrolls;
    }

    /**
     * Obtiene una planilla específica por su identificador único.
     */
    public Optional<Planilla> getPayroll(int idPlanilla) {
        return planillaRepository.findById(idPlanilla);
    }

    /**
     * Obtiene la lista completa de planillas ordenadas de forma descendente por periodo.
     */
    public List<Planilla> getAllPayrolls() {
        return planillaRepository.findAll();
    }

    /**
     * Obtiene la lista de planillas filtradas por un estado en particular.
     */
    public List<Planilla> getPayrollsByStatus(EstadoPlanilla status) {
        return planillaRepository.findByEstadoOrderByFechaInicioDesc(status);
    }

    /**
     * Agrega un ajuste extraordinario (adelanto o descuento) a la planilla y recalcula el neto.
     */
    @Transactional
    public AjustePlanilla addAdjustment(int idPlanilla, TipoAjuste type, BigDecimal amount, String reason) {
        Planilla payroll = planillaRepository.findById(idPlanilla)
                .orElseThrow(() -> new IllegalArgumentException("Planilla no encontrada con ID: " + idPlanilla));

        // Guard temprano: los pagos e importes de planillas cerradas no pueden ser alterados
        if (payroll.getEstado() == EstadoPlanilla.PAGADA) {
            throw new IllegalStateException("No se pueden agregar ajustes a una planilla que ya ha sido marcada como PAGADA.");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del ajuste debe ser mayor a cero.");
        }

        AjustePlanilla adjustment = AjustePlanilla.builder()
                .planilla(payroll)
                .tipo(type)
                .monto(amount)
                .motivo(reason)
                .build();

        payroll.getAjustes().add(adjustment);
        recalculateTotal(payroll);

        planillaRepository.save(payroll);
        return ajustePlanillaRepository.save(adjustment);
    }

    /**
     * Elimina un ajuste extraordinario de la planilla correspondiente y recalcula el neto.
     */
    @Transactional
    public void deleteAdjustment(int idAjuste) {
        AjustePlanilla adjustment = ajustePlanillaRepository.findById(idAjuste)
                .orElseThrow(() -> new IllegalArgumentException("Ajuste de planilla no encontrado con ID: " + idAjuste));

        Planilla payroll = adjustment.getPlanilla();

        // Guard temprano: no se permite eliminar deducciones o adelantos si la planilla ya fue cobrada/pagada
        if (payroll.getEstado() == EstadoPlanilla.PAGADA) {
            throw new IllegalStateException("No se pueden eliminar ajustes de una planilla que ya ha sido marcada como PAGADA.");
        }

        payroll.getAjustes().remove(adjustment);
        recalculateTotal(payroll);

        ajustePlanillaRepository.delete(adjustment);
        planillaRepository.save(payroll);
    }

    /**
     * Finaliza la planilla y la marca como PAGADA, cerrando el ciclo administrativo de la semana.
     */
    @Transactional
    public void markAsPaid(int idPlanilla) {
        Planilla payroll = planillaRepository.findById(idPlanilla)
                .orElseThrow(() -> new IllegalArgumentException("Planilla no encontrada con ID: " + idPlanilla));

        // Guard temprano para evitar reprocesamientos innecesarios
        if (payroll.getEstado() == EstadoPlanilla.PAGADA) {
            return;
        }

        payroll.setEstado(EstadoPlanilla.PAGADA);
        planillaRepository.save(payroll);
    }

    /**
     * Recalcula el total de pago neto de la planilla a partir de los detalles diarios y los ajustes registrados.
     */
    private void recalculateTotal(Planilla payroll) {
        BigDecimal finalTotal = payroll.getDetalles().stream()
                .map(PlanillaDetalle::getPagoDia)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (AjustePlanilla adjustment : payroll.getAjustes()) {
            if (adjustment.getTipo() == TipoAjuste.ADELANTO) {
                // Adelantos suman al pago final de la planilla de forma extraordinaria
                finalTotal = finalTotal.add(adjustment.getMonto());
            } else if (adjustment.getTipo() == TipoAjuste.DESCUENTO) {
                // Descuentos extraordinarios restan al pago final de la planilla
                finalTotal = finalTotal.subtract(adjustment.getMonto());
            }
        }

        // Garantizar que la planilla no resulte con un saldo neto negativo para el empleado
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
        }

        payroll.setTotalPago(finalTotal.setScale(2, RoundingMode.HALF_UP));
    }


    /**
     * Calcula el sueldo diario equivalente según el periodo de pago acordado en el contrato del empleado.
     */
    private BigDecimal calculateDailyRate(Sueldo salary) {
        PeriodoPago period = salary.getPeriodo();
        BigDecimal amount = salary.getSueldo();

        // El usuario indicó dividir entre 6 si es periodo SEMANAL. Si es MENSUAL, se mantiene el divisor estándar de 30 días.
        if (period == PeriodoPago.SEMANAL) {      
            return amount.divide(systemConfigurationService.getDivisorSemanal(), 4, RoundingMode.HALF_UP);
        }
        if (period == PeriodoPago.MENSUAL) {       
            return amount.divide(systemConfigurationService.getDivisorMensual(), 4, RoundingMode.HALF_UP);
        }
        return amount; // DIARIO
    }
    
    
    /**
     * Elimina una planilla de forma permanente si se encuentra en estado GENERADA.
     */
    @Transactional
    public void deletePayroll(int idPlanilla) {
        Planilla payroll = planillaRepository.findById(idPlanilla)
                .orElseThrow(() -> new IllegalArgumentException("Planilla no encontrada con ID: " + idPlanilla));

        // Guard temprano: no se permite eliminar planillas cuyo ciclo administrativo ha sido cerrado/pagado
        if (payroll.getEstado() == EstadoPlanilla.PAGADA) {
            throw new IllegalStateException("No se puede eliminar una planilla que ya ha sido marcada como PAGADA.");
        }

        planillaRepository.delete(payroll);
    }
    /**
     * Obtiene las planillas generadas en una semana específica ordenadas por apellido del empleado.
     */
    public List<Planilla> getPayrollsByStartDate(LocalDate startOfWeek) {
        return planillaRepository.findByFechaInicioOrderByEmpleado_ApellidosAsc(startOfWeek);
    }

    /**
     * Obtiene todas las fechas de inicio únicas de las planillas generadas en el sistema.
     */
    public List<LocalDate> getUniqueStartDates() {
        return planillaRepository.findDistinctFechaInicio();
    }

    /**
     * Calcula la suma total de horas base para todas las planillas de una semana específica.
     */
    public BigDecimal getTotalBaseHoursByWeek(LocalDate startOfWeek) {
        return planillaRepository.sumTotalHorasBaseByFechaInicio(startOfWeek);
    }

    /**
     * Calcula la suma total de horas extra para todas las planillas de una semana específica.
     */
    public BigDecimal getTotalExtraHoursByWeek(LocalDate startOfWeek) {
        return planillaRepository.sumTotalHorasExtraByFechaInicio(startOfWeek);
    }

    /**
     * Calcula la suma del pago neto total para todas las planillas de una semana específica.
     */
    public BigDecimal getTotalPaymentByWeek(LocalDate startOfWeek) {
        return planillaRepository.sumTotalPagoByFechaInicio(startOfWeek);
    }

    /**
     * Cuenta la cantidad de planillas que tienen un estado particular dentro de una semana específica.
     */
    public long countByWeekAndStatus(LocalDate startOfWeek, EstadoPlanilla status) {
        return planillaRepository.countByFechaInicioAndEstado(startOfWeek, status);
    }

    /**
     * Cuenta la cantidad total de planillas generadas para una semana específica.
     */
    public long countByWeek(LocalDate startOfWeek) {
        return planillaRepository.countByFechaInicio(startOfWeek);
    }
    
    /**
     * Obtiene el resumen consolidado de planillas para una semana específica.
     * Agrupa y calcula horas, montos y cantidad de planillas en un DTO.
     */
    public WeeklySummaryDTO getWeeklySummary(LocalDate startOfWeek) {
        BigDecimal totalHorasBase = getTotalBaseHoursByWeek(startOfWeek);
        BigDecimal totalHorasExtra = getTotalExtraHoursByWeek(startOfWeek);
        BigDecimal totalPago = getTotalPaymentByWeek(startOfWeek);
        long totalPlanillas = countByWeek(startOfWeek);
        long planillasPagadas = countByWeekAndStatus(startOfWeek, EstadoPlanilla.PAGADA);
        long planillasPendientes = countByWeekAndStatus(startOfWeek, EstadoPlanilla.GENERADA);

        return WeeklySummaryDTO.builder()
                .totalBaseHours(totalHorasBase)
                .totalExtraHours(totalHorasExtra)
                .totalPayment(totalPago)
                .totalPayrolls(totalPlanillas)
                .paidPayrolls(planillasPagadas)
                .pendingPayrolls(planillasPendientes)
                .build();
    }
    
    /**
     * Obtiene el historial completo de planillas para un empleado determinado.
     * Ordenado de la planilla más reciente a la más antigua.
     */
    public List<Planilla> getPayrollsByEmployee(int idEmpleado) {
        return planillaRepository.findByEmpleado_IdEmpleadoOrderByFechaInicioDesc(idEmpleado);
    }
}

