package com.construplan.admin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.construplan.admin.model.entity.Proyecto;
import com.construplan.admin.repository.ProyectoRepository;

@Service
public class ProyectoService {
	 @Autowired
	    private ProyectoRepository proyectoRepository;

	    public List<Proyecto> listarTodos() {
	        return proyectoRepository.findAll();
	    }

	    public List<Proyecto> listarActivos() {
	        return proyectoRepository.findByActivoTrue();
	    }

	    public List<Proyecto> listarInactivos() {
	        return proyectoRepository.findByActivoFalse();
	    }

	    public Proyecto obtenerPorId(int id) {
	        return proyectoRepository.findById(id)
	                .orElseThrow(() -> new IllegalStateException("Proyecto no encontrado"));
	    }

	    @Transactional
	    public boolean guardar(Proyecto proyecto) {
	        validar(proyecto);
	        if (proyectoRepository.existsByNombreIgnoreCase(proyecto.getNombre()))
	            throw new IllegalStateException("Ya existe un proyecto con ese nombre");

	        proyecto.setActivo(true);
	        proyectoRepository.save(proyecto);
	        return true;
	    }

	    @Transactional
	    public boolean actualizar(Proyecto proyecto) {
	        if (proyecto.getIdProyecto() <= 0)
	            throw new IllegalArgumentException("ID de proyecto inválido");

	        proyectoRepository.findById(proyecto.getIdProyecto())
	                .orElseThrow(() -> new IllegalStateException("Proyecto no encontrado"));

	        validar(proyecto);
	        proyectoRepository.save(proyecto);
	        return true;
	    }

	    @Transactional
	    public boolean activarDesactivar(int id, boolean activo) {
	        Proyecto proyecto = proyectoRepository.findById(id)
	                .orElseThrow(() -> new IllegalStateException("Proyecto no encontrado"));
	        proyecto.setActivo(activo);
	        proyectoRepository.save(proyecto);
	        return true;
	    }

	    @Transactional
	    public boolean eliminar(int id) {
	        proyectoRepository.findById(id)
	                .orElseThrow(() -> new IllegalStateException("Proyecto no encontrado"));
	        proyectoRepository.deleteById(id);
	        return true;
	    }

	    private void validar(Proyecto proyecto) {
	        if (proyecto == null)
	            throw new IllegalArgumentException("El proyecto no puede ser nulo");
	        if (proyecto.getNombre() == null || proyecto.getNombre().trim().isEmpty())
	            throw new IllegalArgumentException("El nombre del proyecto es requerido");
	        if (proyecto.getNombre().length() < 3)
	            throw new IllegalArgumentException("El nombre debe tener al menos 3 caracteres");
	    }
}
