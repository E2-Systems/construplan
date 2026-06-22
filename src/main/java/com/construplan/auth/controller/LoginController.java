package com.construplan.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.construplan.model.entity.Rol;

@Controller
public class LoginController {
	
	@GetMapping("/login") 
	public String login(Authentication authentication) { 
		 // Si ya está autenticado, redirigir al endpoint general de control de dashboards
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
		return "auth/login"; 
		}
	
	/**
	 * Enruta de forma centralizada al usuario autenticado hacia su página de inicio 
	 * correspondiente de acuerdo con los privilegios de su rol asignado.
	 */
	@GetMapping("/dashboard")
	public String redirectToDashboard(Authentication authentication) {
		// Guard temprano: si no hay sesión activa, redirigir a la pantalla de login.
		if (authentication == null || !authentication.isAuthenticated()) {
			return "redirect:/login";
		}

		String authority = authentication.getAuthorities().iterator().next().getAuthority();
		String targetUrl = Rol.fromAuthority(authority)
				.map(Rol::getDashboardUrl)
				.orElse("/login");

		return "redirect:" + targetUrl;
	}
}
