package com.construplan.campo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.construplan.admin.model.entity.Proyecto;
import com.construplan.admin.model.entity.Usuario;
import com.construplan.admin.repository.ProyectoRepository;
import com.construplan.admin.repository.UsuarioRepository;
import com.construplan.campo.model.entity.AsignacionTarea;
import com.construplan.campo.model.entity.Meta;
import com.construplan.campo.model.entity.Modalidad;
import com.construplan.campo.repository.AsignacionTareaRepository;
import com.construplan.campo.repository.MetaRepository;
import com.construplan.empleado.model.entity.Empleado;
import com.construplan.repository.EmpleadoRepository;

@Service
public class AsignacionTareaService {
	 @Autowired
	    private AsignacionTareaRepository asignacionRepository;

	    @Autowired
	    private EmpleadoRepository empleadoRepository;

	    @Autowired
	    private MetaRepository metaRepository;

	    @Autowired
	    private ProyectoRepository proyectoRepository;

	    @Autowired
	    private UsuarioRepository usuarioRepository;

	    // ─── Consultas ────────────────────────────────────────────────────────────

	    public List<AsignacionTarea> listarTodas() {
	        return asignacionRepository.findAll();
	    }

	    public List<AsignacionTarea> listarPorFecha(LocalDate fecha) {
	        return asignacionRepository.findByFecha(fecha);
	    }

	    public List<AsignacionTarea> listarPorEmpleado(int idEmpleado) {
	        return asignacionRepository.findByEmpleadoIdEmpleado(idEmpleado);
	    }

	    public AsignacionTarea obtenerPorId(int id) {
	        return asignacionRepository.findById(id)
	                .orElseThrow(() -> new IllegalStateException("Asignación no encontrada"));
	    }

	    // ─── Crear ────────────────────────────────────────────────────────────────

	    @Transactional
	    public boolean crearAsignacion(int idEmpleado, int idMeta, int idProyecto,
	                                   LocalDate fecha, Modalidad modalidad) {
	        validarDatos(idEmpleado, idMeta, idProyecto, fecha);

	        Empleado empleado = empleadoRepository.findById(idEmpleado)
	                .orElseThrow(() -> new IllegalStateException("Empleado no encontrado"));
	        Meta meta = metaRepository.findById(idMeta)
	                .orElseThrow(() -> new IllegalStateException("Meta no encontrada"));
	        Proyecto proyecto = proyectoRepository.findById(idProyecto)
	                .orElseThrow(() -> new IllegalStateException("Proyecto no encontrado"));
	        Usuario asignador = obtenerUsuarioActual();

	        AsignacionTarea asignacion = AsignacionTarea.builder()
	                .empleado(empleado)
	                .meta(meta)
	                .proyecto(proyecto)
	                .asignador(asignador)
	                .fecha(fecha)
	                .modalidad(modalidad)
	                .build();

	        asignacionRepository.save(asignacion);
	        return true;
	    }

	    // ─── Actualizar ───────────────────────────────────────────────────────────

	    @Transactional
	    public boolean actualizarAsignacion(int idAsignacion, int idMeta,
	                                        int idProyecto, LocalDate fecha,
	                                        Modalidad modalidad) {
	        AsignacionTarea asignacion = asignacionRepository.findById(idAsignacion)
	                .orElseThrow(() -> new IllegalStateException("Asignación no encontrada"));

	        Meta meta = metaRepository.findById(idMeta)
	                .orElseThrow(() -> new IllegalStateException("Meta no encontrada"));
	        Proyecto proyecto = proyectoRepository.findById(idProyecto)
	                .orElseThrow(() -> new IllegalStateException("Proyecto no encontrado"));

	        asignacion.setMeta(meta);
	        asignacion.setProyecto(proyecto);
	        asignacion.setFecha(fecha);
	        asignacion.setModalidad(modalidad);

	        asignacionRepository.save(asignacion);
	        return true;
	    }

	    // ─── Eliminar ─────────────────────────────────────────────────────────────

	    @Transactional
	    public boolean eliminarAsignacion(int id) {
	        asignacionRepository.findById(id)
	                .orElseThrow(() -> new IllegalStateException("Asignación no encontrada"));
	        asignacionRepository.deleteById(id);
	        return true;
	    }

	    // ─── Lo que tenía el service anterior — redefinido correctamente ──────────

	    @Transactional
	    public boolean finalizarTarea(int idAsignacion) {
	        AsignacionTarea asignacion = asignacionRepository.findById(idAsignacion)
	                .orElseThrow(() -> new IllegalStateException("Asignación no encontrada"));
	        asignacion.setHoraMetaCompletada(java.time.LocalTime.now());
	        asignacionRepository.save(asignacion);
	        return true;
	    }

	    // ─── Utilidades ───────────────────────────────────────────────────────────

	    private void validarDatos(int idEmpleado, int idMeta, int idProyecto, LocalDate fecha) {
	        if (idEmpleado <= 0)
	            throw new IllegalArgumentException("Empleado inválido");
	        if (idMeta <= 0)
	            throw new IllegalArgumentException("Meta inválida");
	        if (idProyecto <= 0)
	            throw new IllegalArgumentException("Proyecto inválido");
	        if (fecha == null)
	            throw new IllegalArgumentException("La fecha es requerida");
	    }

	    private Usuario obtenerUsuarioActual() {
	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	        return usuarioRepository.findByUsername(auth.getName())
	                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
	    }
}
