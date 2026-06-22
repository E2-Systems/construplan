package com.construplan.admin.service;


import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.construplan.admin.model.entity.Configuracion;
import com.construplan.admin.repository.ConfiguracionRepository;

import jakarta.annotation.PostConstruct;

/**
 * Servicio encargado de gestionar los parámetros de configuración del sistema persistidos en BD.
 * Implementa almacenamiento en caché local para optimizar el rendimiento de los cálculos y
 * realiza una inicialización de respaldo con valores por defecto.
 */
@Service
public class SystemConfigurationService {

    @Autowired
    private ConfiguracionRepository configuracionRepository;

    private static final int CONFIG_ID = 1;

    private BigDecimal cachedJornadaEstandar = BigDecimal.valueOf(8.5);
    private BigDecimal cachedMultiplicadorHorasExtras = BigDecimal.valueOf(1.25);
    private BigDecimal cachedDivisorSemanal = BigDecimal.valueOf(6);
    private BigDecimal cachedDivisorMensual = BigDecimal.valueOf(30);

    @PostConstruct
    public void init() {
        try {
            Configuracion config = getOrCreateConfig();
            updateCache(config);
        } catch (Exception e) {
            // Si la base de datos no está disponible o la tabla no ha sido creada,
            // se mantiene el uso de las constantes en caché por defecto.
        }
    }

    private Configuracion getOrCreateConfig() {
        return configuracionRepository.findById(CONFIG_ID)
                .orElseGet(() -> {
                    Configuracion defaultConfig = Configuracion.builder()
                            .idConfiguracion(CONFIG_ID)
                            .jornadaEstandar(BigDecimal.valueOf(8.5))
                            .multiplicadorHorasExtras(BigDecimal.valueOf(1.25))
                            .divisorSemanal(BigDecimal.valueOf(6))
                            .divisorMensual(BigDecimal.valueOf(30))
                            .build();
                    return configuracionRepository.save(defaultConfig);
                });
    }

    private void updateCache(Configuracion config) {
        this.cachedJornadaEstandar = config.getJornadaEstandar();
        this.cachedMultiplicadorHorasExtras = config.getMultiplicadorHorasExtras();
        this.cachedDivisorSemanal = config.getDivisorSemanal();
        this.cachedDivisorMensual = config.getDivisorMensual();
    }

    public BigDecimal getJornadaEstandar() {
        return cachedJornadaEstandar;
    }

    public BigDecimal getMultiplicadorHorasExtras() {
        return cachedMultiplicadorHorasExtras;
    }

    public BigDecimal getDivisorSemanal() {
        return cachedDivisorSemanal;
    }

    public BigDecimal getDivisorMensual() {
        return cachedDivisorMensual;
    }

    public Configuracion getActiveConfiguration() {
        try {
            return getOrCreateConfig();
        } catch (Exception e) {
            return Configuracion.builder()
                    .idConfiguracion(CONFIG_ID)
                    .jornadaEstandar(cachedJornadaEstandar)
                    .multiplicadorHorasExtras(cachedMultiplicadorHorasExtras)
                    .divisorSemanal(cachedDivisorSemanal)
                    .divisorMensual(cachedDivisorMensual)
                    .build();
        }
    }

    @Transactional
    public void saveConfiguration(Configuracion config) {
        config.setIdConfiguracion(CONFIG_ID);
        Configuracion saved = configuracionRepository.save(config);
        updateCache(saved);
    }
}
