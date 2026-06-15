package com.construplan.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
	
	@GetMapping("/login") 
	public String login(Authentication authentication) { 
		 // Si ya está autenticado, redirigir 
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
		return "auth/login"; 
		}
}
