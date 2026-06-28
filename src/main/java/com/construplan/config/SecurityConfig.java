package com.construplan.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.construplan.admin.service.UsuarioDetailsService;
import com.construplan.model.entity.Rol;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Autowired
	private UsuarioDetailsService usuarioDetailsService;

	// ─── Beans ────────────────────────────────────────────────────────────────

	@Bean
	// Para pruebas (texto plano, inseguro):
	public PasswordEncoder passwordEncoder() {
		//return NoOpPasswordEncoder.getInstance();
		return new BCryptPasswordEncoder();
	}
	// Para producción (encriptado seguro):
	// public BCryptPasswordEncoder passwordEncoder() {
	// return new BCryptPasswordEncoder();
//}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(usuarioDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	// ─── Handlers ─────────────────────────────────────────────────────────────

	@Bean
	public AuthenticationSuccessHandler successHandler() {
		return (request, response, authentication) -> {
			String authority = authentication.getAuthorities().iterator().next().getAuthority();

			String url = Rol.fromAuthority(authority).map(Rol::getDashboardUrl).orElse("/dashboard");

			response.sendRedirect(request.getContextPath() + url);
		};
	}

	@Bean
	public AuthenticationFailureHandler failureHandler() {
		return (request, response, exception) -> {
			 String mensaje;
		        if (exception.getMessage().contains("inactivo") ||
		            exception.getMessage().contains("empleado")) {
		            mensaje = "cuenta_inactiva";
		        } else {
		            mensaje = "credenciales_invalidas";
		        }

			response.sendRedirect(request.getContextPath() + "/login?error=" + mensaje);
		};
	}
	// ─── Security Filter Chain ────────────────────────────────────────────────

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authenticationProvider(authenticationProvider())

				.authorizeHttpRequests(auth -> auth
						// Recursos públicos
						.requestMatchers("/login", "/css/**", "/js/**", "/img/**", "/webjars/**").permitAll()
						// Rutas por rol — usa el authority del enum
						.requestMatchers("/admin/**").hasAuthority(Rol.ADMIN.getAuthority())
						.requestMatchers("/oficina/**").hasAuthority(Rol.OFICINA.getAuthority())
						.requestMatchers("/campo/**").hasAuthority(Rol.CAMPO.getAuthority())
						.requestMatchers("/empleado/**").hasAuthority(Rol.EMPLEADO.getAuthority())
						// Cualquier otra ruta requiere autenticación
						.anyRequest().authenticated())

				.formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login")
						.successHandler(successHandler()).failureHandler(failureHandler()).permitAll())

				.logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?mensaje=sesion_cerrada")
						.invalidateHttpSession(true).deleteCookies("JSESSIONID").permitAll())

				.sessionManagement(session -> session.maximumSessions(1) // Un solo login por usuario
						.expiredUrl("/login?mensaje=sesion_expirada") // Si la sesión expira
				);

		return http.build();
	}
}
