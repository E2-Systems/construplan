package com.construplan.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.construplan.admin.model.entity.Configuracion;
import com.construplan.admin.model.entity.Usuario;
import com.construplan.admin.repository.UsuarioRepository;
import com.construplan.admin.service.SystemConfigurationService;
import com.construplan.admin.service.UsuarioService;
import com.construplan.empleado.model.entity.Categoria;
import com.construplan.empleado.model.entity.Empleado;
import com.construplan.empleado.service.EmpleadoService;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	 @Autowired
	    private UsuarioRepository usuarioRepository;
	 
	 @Autowired
	    private UsuarioService usuarioService;
	 
	 @Autowired
	    private EmpleadoService empleadoService;

	 @Autowired
	    private SystemConfigurationService systemConfigurationService;
	 
	@GetMapping("/dashboard") 
	public String dashboard(  Authentication authentication,            Model model) { 
		var usuarios = usuarioRepository.findAll();

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("totalUsuarios", usuarios.size());

        model.addAttribute("usuariosActivos",
                usuarios.stream().filter(u -> u.isActivo()).count());

        model.addAttribute("usuariosInactivos",
                usuarios.stream().filter(u -> !u.isActivo()).count());

        model.addAttribute("countEmpleados",
                usuarios.stream().filter(u -> u.getRol().name().equals("EMPLEADO")).count());

        model.addAttribute("countCampo",
                usuarios.stream().filter(u -> u.getRol().name().equals("CAMPO")).count());

        model.addAttribute("countOficina",
                usuarios.stream().filter(u -> u.getRol().name().equals("OFICINA")).count());

        model.addAttribute("countAdmin",
                usuarios.stream().filter(u -> u.getRol().name().equals("ADMIN")).count());

		return "admin/dashboard"; 
	}

	// Se define este mapping para presentar la lista completa de usuarios del sistema en la vista Thymeleaf
	@GetMapping("/usuarios")
	public String listarUsuarios(Model model) {
		model.addAttribute("usuarios", usuarioService.listarTodos());
		return "admin/usuarios";
	}

	// Se utiliza POST con tokens CSRF provistos por Spring Security para resguardar la acción contra vulnerabilidades web
	@PostMapping("/usuarios/{id}/estado")
	public String cambiarEstado(@PathVariable int id, @RequestParam boolean activo, RedirectAttributes redirectAttrs) {
		try {
			usuarioService.cambiarEstado(id, activo);
			redirectAttrs.addFlashAttribute("mensaje", activo ? "Usuario activado exitosamente" : "Usuario desactivado exitosamente");
		} catch (Exception e) {
			redirectAttrs.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/admin/usuarios";	
	}
	
	// Se despliega el formulario de registro de usuario inicializando un objeto vacío en el modelo
		@GetMapping("/usuarios/nuevo")
		public String formularioNuevo(Model model) {
			model.addAttribute("usuario", new Usuario());
			model.addAttribute("action", "create");
			return "admin/usuario-form";
		}

		// Se recupera el usuario por su ID para presentarlo en el formulario de edición
		@GetMapping("/usuarios/{id}/editar")
		public String formularioEditar(@PathVariable int id, Model model, RedirectAttributes redirectAttrs) {
			try {
				model.addAttribute("usuario", usuarioService.obtenerPorId(id));
				model.addAttribute("action", "update");
				return "admin/usuario-form";
			} catch (Exception e) {
				redirectAttrs.addFlashAttribute("error", e.getMessage());
				return "redirect:/admin/usuarios";
			}
		}

		// Se recibe y valida la información del nuevo usuario antes de persistirlo
		@PostMapping("/usuarios")
		public String crear(@ModelAttribute Usuario usuario, Model model, RedirectAttributes redirectAttrs) {
			try {
				usuario.setActivo(true);
				usuarioService.registrarUsuario(usuario);
				redirectAttrs.addFlashAttribute("mensaje", "Usuario creado exitosamente");
				return "redirect:/admin/usuarios";
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
				model.addAttribute("usuario", usuario);
				model.addAttribute("action", "create");
				return "admin/usuario-form";
			}
		}

		// Se procesa la actualización de los datos del usuario existente
		@PostMapping("/usuarios/{id}")
		public String actualizar(@PathVariable int id, @ModelAttribute Usuario usuario, Model model, RedirectAttributes redirectAttrs) {
			usuario.setId(id);
			try {
				Usuario actual = usuarioService.obtenerPorId(id);
				usuario.setPassword(actual.getPassword());
				usuario.setFechaCreacion(actual.getFechaCreacion());
				usuarioService.actualizarUsuario(usuario);
				redirectAttrs.addFlashAttribute("mensaje", "Usuario actualizado exitosamente");
				return "redirect:/admin/usuarios";
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
				model.addAttribute("usuario", usuario);
				model.addAttribute("action", "update");
				return "admin/usuario-form";
			}
		}
		// ─── Mantenimiento de Empleados ───────────────────────────────────────────

		/**
		 * Muestra la lista de empleados contratados para permitir al administrador gestionar sus perfiles.
		 */
		@GetMapping("/empleados")
		public String listEmployees(Model model) {
			model.addAttribute("empleados", empleadoService.listarTodos());
			return "admin/empleados";
		}

		/**
		 * Muestra el formulario para registrar un nuevo empleado inicializando un modelo vacío.
		 */
		@GetMapping("/empleados/nuevo")
		public String showCreateEmployeeForm(Model model) {
			model.addAttribute("empleado", new Empleado());
			model.addAttribute("categorias", Categoria.values());
			model.addAttribute("usuariosDisponibles", usuarioService.obtenerUsuariosEmpleadoSinAsociar());
			model.addAttribute("action", "create");
			return "admin/empleado-form";
		}

		/**
		 * Obtiene el empleado por su identificador para cargar sus datos actuales en el formulario de edición.
		 */
		@GetMapping("/empleados/{id}/editar")
		public String showEditEmployeeForm(@PathVariable int id, Model model, RedirectAttributes redirectAttrs) {
			try {
				Empleado employee = empleadoService.obtenerPorId(id);
				model.addAttribute("empleado", employee);
				model.addAttribute("categorias", Categoria.values());
				
				// Es necesario añadir tanto los usuarios sin asociar como el usuario que ya tiene asignado actualmente
				// para que no se pierda la selección del dropdown en la vista Thymeleaf.
				var availableUsers = new java.util.ArrayList<>(usuarioService.obtenerUsuariosEmpleadoSinAsociar());
				if (employee.getUsuario() != null) {
					availableUsers.add(employee.getUsuario());
				}
				model.addAttribute("usuariosDisponibles", availableUsers);
				model.addAttribute("action", "update");
				return "admin/empleado-form";
			} catch (Exception e) {
				redirectAttrs.addFlashAttribute("error", e.getMessage());
				return "redirect:/admin/empleados";
			}
		}

		/**
		 * Procesa la persistencia de la creación o actualización de los datos del empleado.
		 */
		@PostMapping("/empleados")
		public String saveEmployee(@ModelAttribute Empleado empleado, Model model, RedirectAttributes redirectAttrs) {
			try {
				if (empleado.getIdEmpleado() != null && empleado.getIdEmpleado() > 0) {
					Empleado existing = empleadoService.obtenerPorId(empleado.getIdEmpleado());
					
					// Se realiza mapeo manual de las propiedades editables para respetar la inmutabilidad de campos 
					// internos y evitar sobreescritura accidental de atributos no enviados desde el formulario.
					existing.setNombres(empleado.getNombres());
					existing.setApellidos(empleado.getApellidos());
					existing.setDni(empleado.getDni());
					existing.setCategoria(empleado.getCategoria());
					existing.setDireccion(empleado.getDireccion());
					existing.setTelefono(empleado.getTelefono());
					existing.setFechaNacimiento(empleado.getFechaNacimiento());
					existing.setBanco(empleado.getBanco());
					existing.setCuentaBancaria(empleado.getCuentaBancaria());
					existing.setUsuario(empleado.getUsuario());
					
					empleadoService.actualizar(existing);
					redirectAttrs.addFlashAttribute("mensaje", "Empleado actualizado exitosamente");
				} else {
					empleado.setActivo(true);
					empleadoService.crearEmpleadoSinUsuario(empleado);
					redirectAttrs.addFlashAttribute("mensaje", "Empleado creado exitosamente");
				}
				return "redirect:/admin/empleados";
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
				model.addAttribute("empleado", empleado);
				model.addAttribute("categorias", Categoria.values());
				var availableUsers = new java.util.ArrayList<>(usuarioService.obtenerUsuariosEmpleadoSinAsociar());
				if (empleado.getUsuario() != null) {
					availableUsers.add(empleado.getUsuario());
				}
				model.addAttribute("usuariosDisponibles", availableUsers);
				model.addAttribute("action", empleado.getIdEmpleado() != null ? "update" : "create");
				return "admin/empleado-form";
			}
		}

		/**
		 * Cambia el estado de activación del empleado para restringir o permitir su participación en tareas.
		 */
		@PostMapping("/empleados/{id}/estado")
		public String changeEmployeeStatus(@PathVariable int id, @RequestParam boolean activo, RedirectAttributes redirectAttrs) {
			try {
				empleadoService.activarDesactivar(id, activo);
				redirectAttrs.addFlashAttribute("mensaje", activo ? "Empleado activado exitosamente" : "Empleado desactivado exitosamente");
			} catch (Exception e) {
				redirectAttrs.addFlashAttribute("error", e.getMessage());
			}
			return "redirect:/admin/empleados";
		}

		// ─── Configuración Global del Sistema ─────────────────────────────────────

		/**
		 * Carga el formulario de configuración cargando los parámetros globales de la base de datos.
		 */
		@GetMapping("/configuracion")
		public String showConfigurationForm(Model model) {
			model.addAttribute("configuracion", systemConfigurationService.getActiveConfiguration());
			return "admin/configuracion";
		}

		/**
		 * Guarda las modificaciones realizadas a la configuración global y actualiza la caché del servicio.
		 */
		@PostMapping("/configuracion")
		public String saveConfiguration(@ModelAttribute Configuracion configuracion, RedirectAttributes redirectAttrs) {
			try {
				systemConfigurationService.saveConfiguration(configuracion);
				redirectAttrs.addFlashAttribute("mensaje", "Configuración del sistema actualizada exitosamente");
			} catch (Exception e) {
				redirectAttrs.addFlashAttribute("error", "Error al actualizar la configuración: " + e.getMessage());
			}
			return "redirect:/admin/configuracion";
		}
}
