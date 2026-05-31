package com.construplan.security;

import java.io.IOException;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomSuccessHandler
implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		Collection<? extends GrantedAuthority> roles = authentication.getAuthorities(); 
		for (GrantedAuthority rol : roles) { 
			if (rol.getAuthority().equals("ROLE_ADMIN")) {
				response.sendRedirect("/admin/dashboard");
				return;
			}

			if (rol.getAuthority().equals("ROLE_EMPLEADO")) {
				response.sendRedirect("/empleado/dashboard");
				return;
			}
			if (rol.getAuthority().equals("ROLE_CAMPO")) {
				response.sendRedirect("/campo/dashboard");
				return;
			}
			if (rol.getAuthority().equals("ROLE_OFICINA")) {
				response.sendRedirect("/oficina/dashboard");
				return;
			}
		}
		
	}

}
