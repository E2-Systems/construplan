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

import com.construplan.admin.model.entity.Usuario;
import com.construplan.admin.repository.UsuarioRepository;
import com.construplan.admin.service.UsuarioService;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	 @Autowired
	    private UsuarioRepository usuarioRepository;
	 
	 @Autowired
	    private UsuarioService usuarioService;
	 
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
}
