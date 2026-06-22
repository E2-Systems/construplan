package com.construplan.empleado.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.construplan.admin.model.entity.Usuario;
import com.construplan.admin.repository.UsuarioRepository;
import com.construplan.empleado.model.entity.Categoria;
import com.construplan.empleado.model.entity.Empleado;
import com.construplan.empleado.repository.EmpleadoRepository;
import com.construplan.model.entity.Rol;
import com.construplan.oficina.model.entity.PeriodoPago;
import com.construplan.oficina.service.SueldoService;

@Service
public class EmpleadoService {
	@Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SueldoService sueldoService;
    
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

    public List<Empleado> buscarPorCategoria(Categoria categoria) {
        if (categoria == null)
            throw new IllegalArgumentException("Categoría no puede estar vacía");
        return empleadoRepository.findByCategoria(categoria);
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
    public boolean actualizarCategoria(int idEmpleado, Categoria nuevaCategoria) {
        if (nuevaCategoria == null)
            throw new IllegalArgumentException("Categoría no puede estar vacía");

        Empleado empleado = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new IllegalStateException("Empleado no encontrado"));

        empleado.setCategoria(nuevaCategoria);
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
    	return empleadoRepository.findByUsuario_Username(username)
                .orElseThrow(() -> new IllegalStateException("Empleado no encontrado"));
    }
    

    /**
     * Busca un empleado por username sin lanzar excepción.
     * Retorna Optional vacío si no existe registro en la tabla empleado.
     */
    public Optional<Empleado> buscarOptionalPorUsername(String username) {
        return empleadoRepository.findByUsuario_Username(username);
    }

    /**
     * Crea el registro inicial en la tabla empleado para un usuario que acaba de registrarse.
     * Se requieren nombres, apellidos y DNI como datos mínimos obligatorios.
     */
    @Transactional
    public Empleado crearRegistroInicial(String username, String nombres, String apellidos,
                                         String dni, String direccion, String telefono,
                                         LocalDate fechaNacimiento, String banco,
                                         String cuentaBancaria) {
        // Validar que no exista ya un empleado con ese username
        if (empleadoRepository.findByUsuario_Username(username).isPresent()) {
            throw new IllegalStateException("Ya existe un registro de empleado para este usuario");
        }

        // Validar que el DNI no esté duplicado
        if (empleadoRepository.findByDni(dni).isPresent()) {
            throw new IllegalStateException("Ya existe un empleado registrado con ese DNI");
        }

        // Obtener el usuario asociado
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        Empleado empleado = Empleado.builder()
                .usuario(usuario)
                .nombres(nombres)
                .apellidos(apellidos)
                .dni(dni)
                .direccion(direccion)
                .telefono(telefono)
                .fechaNacimiento(fechaNacimiento)
                .banco(banco)
                .cuentaBancaria(cuentaBancaria)
                .activo(true)
                .build();

        return empleadoRepository.save(empleado);
    }
    
    
 // ─── Perfil del empleado ─────────────────────────────────────────────────

    /**
     * Actualiza únicamente los campos editables del perfil del empleado.
     * Los campos críticos (nombres, apellidos, dni, categoría, activo) no se modifican.
     */
    @Transactional
    public Empleado actualizarPerfil(int idEmpleado, String direccion, String telefono,
                                     LocalDate fechaNacimiento, String banco,
                                     String cuentaBancaria) {
        Empleado existente = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new IllegalStateException("Empleado no encontrado"));

        existente.setDireccion(direccion);
        existente.setTelefono(telefono);
        existente.setFechaNacimiento(fechaNacimiento);
        existente.setBanco(banco);
        existente.setCuentaBancaria(cuentaBancaria);

        return empleadoRepository.save(existente);
    }
    
    

    /**
     * Actualiza la categoría del empleado y registra un nuevo acuerdo de sueldo
     * en el historial salarial llamando a SueldoService.
     */
    @Transactional
    public void actualizarCategoriaYSueldo(int idEmpleado, Categoria categoria, BigDecimal sueldo,
                                           PeriodoPago periodo, LocalDate fechaInicio) {
        Empleado empleado = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new IllegalStateException("Empleado no encontrado"));

        empleado.setCategoria(categoria);
        empleadoRepository.save(empleado);

        sueldoService.registrarNuevoSueldo(idEmpleado, sueldo, periodo, fechaInicio);
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
