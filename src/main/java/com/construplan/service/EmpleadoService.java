package com.construplan.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.construplan.empleado.model.entity.Empleado;
import com.construplan.model.entity.Rol;
import com.construplan.model.entity.Usuario;
import com.construplan.repository.EmpleadoRepository;
import com.construplan.repository.UsuarioRepository;

@Service
public class EmpleadoService {
	@Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ─── Consultas ────────────────────────────────────────────────────────────

    public Empleado obtenerPorId(int id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Empleado no encontrado"));
    }

    public Empleado buscarPorDni(String dni) {
        if (dni == null || dni.trim().isEmpty())
            throw new IllegalArgumentException("DNI no puede estar vacío");
        return empleadoRepository.findByDni(dni.trim())
                .orElseThrow(() -> new IllegalStateException("Empleado no encontrado con DNI: " + dni));
    }

    public Empleado buscarPorIdUsuario(int idUsuario) {
        return empleadoRepository.findByUsuario_Id(idUsuario)
                .orElseThrow(() -> new IllegalStateException("No existe empleado asociado al usuario"));
    }

    public List<Empleado> listarTodos() {
        return empleadoRepository.findAll();
    }

    public List<Empleado> listarActivos() {
        return empleadoRepository.findByActivoTrue();
    }

    public List<Empleado> listarInactivos() {
        return empleadoRepository.findByActivoFalse();
    }

    public List<Empleado> listarEmpleadosSinUsuario() {
        return empleadoRepository.findByUsuarioIsNull();
    }

    public List<Empleado> listarEmpleadosConUsuario() {
        return empleadoRepository.findByUsuarioIsNotNull();
    }

    public List<Empleado> buscarPorCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty())
            throw new IllegalArgumentException("Categoría no puede estar vacía");
        return empleadoRepository.findByCategoria(categoria.trim());
    }

    // ─── Conteos ──────────────────────────────────────────────────────────────

    public long contarEmpleados() {
        return empleadoRepository.count();
    }

    public long contarEmpleadosActivos() {
        return empleadoRepository.countByActivoTrue();
    }

    public long contarEmpleadosSinUsuario() {
        return empleadoRepository.countByUsuarioIsNull();
    }

    public long contarEmpleadosConUsuario() {
        return empleadoRepository.countByUsuarioIsNotNull();
    }

    // ─── Creación ─────────────────────────────────────────────────────────────

    @Transactional
    public Empleado crearEmpleadoSinUsuario(Empleado empleado) {
        validarEmpleado(empleado);
        if (empleadoRepository.findByDni(empleado.getDni()).isPresent())
            throw new IllegalStateException("Ya existe un empleado con ese DNI");

      //  empleado.setFechaRegistro(LocalDateTime.now());
        empleado.setActivo(true);
        return empleadoRepository.save(empleado);
    }

    @Transactional
    public Empleado crearEmpleadoConUsuario(Empleado empleado, String username, String password) {
        validarEmpleado(empleado);
        if (empleadoRepository.findByDni(empleado.getDni()).isPresent())
            throw new IllegalStateException("Ya existe un empleado con ese DNI");
        if (usuarioRepository.existsByUsername(username))
            throw new IllegalStateException("El nombre de usuario ya existe");

        Usuario usuario = Usuario.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .rol(Rol.EMPLEADO)
                .activo(true)
                .build();
        usuarioRepository.save(usuario);

        empleado.setUsuario(usuario);
        //empleado.setFechaRegistro(LocalDateTime.now());
        empleado.setActivo(true);
        return empleadoRepository.save(empleado);
    }

    @Transactional
    public boolean asignarUsuarioAEmpleado(int idEmpleado, String username, String password) {
        Empleado empleado = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new IllegalStateException("Empleado no encontrado"));

        if (empleado.getUsuario() != null)
            throw new IllegalStateException("El empleado ya tiene usuario asignado");
        if (usuarioRepository.existsByUsername(username))
            throw new IllegalStateException("El nombre de usuario ya existe");

        Usuario usuario = Usuario.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .rol(Rol.EMPLEADO)
                .activo(true)
                .build();
        usuarioRepository.save(usuario);

        empleado.setUsuario(usuario);
        empleadoRepository.save(empleado);
        return true;
    }

    // ─── Actualización ────────────────────────────────────────────────────────

    @Transactional
    public Empleado actualizar(Empleado empleado) {
        if (empleado.getIdEmpleado() <= 0)
            throw new IllegalArgumentException("ID de empleado inválido");

        empleadoRepository.findById(empleado.getIdEmpleado())
                .orElseThrow(() -> new IllegalStateException("Empleado no encontrado"));

        return empleadoRepository.save(empleado);
    }

    @Transactional
    public boolean actualizarCategoria(int idEmpleado, String nuevaCategoria) {
        if (nuevaCategoria == null || nuevaCategoria.trim().isEmpty())
            throw new IllegalArgumentException("Categoría no puede estar vacía");

        Empleado empleado = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new IllegalStateException("Empleado no encontrado"));

        empleado.setCategoria(nuevaCategoria.trim());
        empleadoRepository.save(empleado);
        return true;
    }

    @Transactional
    public boolean activarDesactivar(int id, boolean activo) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Empleado no encontrado"));

        empleado.setActivo(activo);
        empleadoRepository.save(empleado);
        return true;
    }

    public Empleado buscarPorUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        return empleadoRepository.findByUsuario_Id(usuario.getId())
                .orElseThrow(() -> new IllegalStateException("Empleado no encontrado"));
    }
    
    // ─── Validación interna ───────────────────────────────────────────────────

    private void validarEmpleado(Empleado empleado) {
        if (empleado == null)
            throw new IllegalArgumentException("Empleado no puede ser nulo");
        if (empleado.getNombres() == null || empleado.getNombres().trim().isEmpty())
            throw new IllegalArgumentException("El nombre es requerido");
        if (empleado.getApellidos() == null || empleado.getApellidos().trim().isEmpty())
            throw new IllegalArgumentException("Los apellidos son requeridos");
        if (empleado.getDni() == null || empleado.getDni().trim().isEmpty())
            throw new IllegalArgumentException("El DNI es requerido");
    }
}
