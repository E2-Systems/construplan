package com.construplan.campo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.construplan.campo.model.entity.Tarea;
import com.construplan.campo.repository.TareaRepository;

import java.util.List;

@Service
public class TareaService {
	   @Autowired
	    private TareaRepository tareaRepository;

	    public List<Tarea> listarTodas() {
	        return tareaRepository.findAll();
	    }

	    public Tarea obtenerPorId(int id) {
	        if (id <= 0)
	            throw new IllegalArgumentException("ID de tarea inválido");
	        return tareaRepository.findById(id)
	                .orElseThrow(() -> new IllegalStateException("Tarea no encontrada"));
	    }

	    public List<Tarea> buscarPorNombre(String nombre) {
	        if (nombre == null || nombre.trim().isEmpty())
	            throw new IllegalArgumentException("El nombre no puede estar vacío");
	        return tareaRepository.findByNombreContainingIgnoreCase(nombre.trim());
	    }

	    @Transactional
	    public boolean registrarTarea(Tarea tarea) {
	        validarTarea(tarea);
	        if (tareaRepository.existsByNombreIgnoreCase(tarea.getNombre()))
	            throw new IllegalStateException("Ya existe una tarea con ese nombre");

	        tareaRepository.save(tarea);
	        return true;
	    }

	    @Transactional
	    public boolean actualizarTarea(Tarea tarea) {
	        if (tarea.getIdTarea() <= 0)
	            throw new IllegalArgumentException("ID de tarea inválido");

	        tareaRepository.findById(tarea.getIdTarea())
	                .orElseThrow(() -> new IllegalStateException("Tarea no encontrada"));

	        validarTarea(tarea);
	        tareaRepository.save(tarea);
	        return true;
	    }

	    @Transactional
	    public boolean eliminarTarea(int id) {
	        if (id <= 0)
	            throw new IllegalArgumentException("ID de tarea inválido");

	        tareaRepository.findById(id)
	                .orElseThrow(() -> new IllegalStateException("Tarea no encontrada"));

	        tareaRepository.deleteById(id);
	        return true;
	    }

	    private void validarTarea(Tarea tarea) {
	        if (tarea == null)
	            throw new IllegalArgumentException("La tarea no puede ser nula");
	        if (tarea.getNombre() == null || tarea.getNombre().trim().isEmpty())
	            throw new IllegalArgumentException("El nombre de la tarea es requerido");
	        if (tarea.getNombre().length() < 4)
	            throw new IllegalArgumentException("El nombre debe tener al menos 4 caracteres");
	        if (tarea.getUnidadMedida() == null || tarea.getUnidadMedida().trim().isEmpty())
	            throw new IllegalArgumentException("La unidad de medida es requerida");
	    }
}
