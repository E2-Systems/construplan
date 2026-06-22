package com.construplan.campo.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.construplan.campo.model.entity.Meta;
import com.construplan.campo.repository.MetaRepository;
import com.construplan.campo.repository.TareaRepository;

@Service
public class MetaService {

    @Autowired
    private MetaRepository metaRepository;

    @Autowired
    private TareaRepository tareaRepository;

    public List<Meta> listarTodas() {
        return metaRepository.findAll();
    }

    public Meta obtenerPorId(int idMeta) {
        if (idMeta <= 0)
            throw new IllegalArgumentException("El id debe ser mayor a cero");
        return metaRepository.findById(idMeta)
                .orElseThrow(() -> new IllegalStateException("Meta no encontrada"));
    }

    public List<Meta> listarPorTarea(int idTarea) {
        return metaRepository.findByTareaIdTarea(idTarea);
    }

    @Transactional
    public boolean registrarMeta(Meta meta) {
        validarMeta(meta);
        metaRepository.save(meta);
        return true;
    }

    @Transactional
    public boolean actualizarMeta(Meta meta) {
        if (meta.getIdMeta() <= 0)
            throw new IllegalArgumentException("ID de meta inválido");
        metaRepository.findById(meta.getIdMeta())
                .orElseThrow(() -> new IllegalStateException("Meta no encontrada"));
        validarMeta(meta);
        metaRepository.save(meta);
        return true;
    }

    @Transactional
    public boolean eliminarMeta(int idMeta) {
        if (idMeta <= 0)
            throw new IllegalArgumentException("ID de meta inválido");
        metaRepository.findById(idMeta)
                .orElseThrow(() -> new IllegalStateException("Meta no encontrada"));
        metaRepository.deleteById(idMeta);
        return true;
    }

    private void validarMeta(Meta meta) {
        if (meta == null)
            throw new IllegalArgumentException("Meta no puede ser nula");

        if (meta.getCantidad() == null || meta.getCantidad().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");

        if (meta.getHorasEquivalentes() == null || meta.getHorasEquivalentes().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Las horas equivalentes deben ser mayores a cero");

        if (meta.getTarea() == null)
            throw new IllegalArgumentException("La tarea es requerida");
    }
}