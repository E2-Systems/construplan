package com.construplan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.construplan.repository.UsuarioRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	 @Autowired
	    private UsuarioRepository usuarioRepository;
	 
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
}
