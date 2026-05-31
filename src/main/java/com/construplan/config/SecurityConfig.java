package com.construplan.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.construplan.security.CustomSuccessHandler;
import com.construplan.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {
	@Autowired
	private CustomUserDetailsService userDetailsService;
	@Autowired
	private CustomSuccessHandler successHandler;

	@Bean	
	 // Para pruebas (texto plano, inseguro):
	public PasswordEncoder passwordEncoder() {	
	    return NoOpPasswordEncoder.getInstance();  	    
	}
	  // Para producción (encriptado seguro):
	//public BCryptPasswordEncoder passwordEncoder() {
	// return new BCryptPasswordEncoder();
//}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth.requestMatchers("/login").permitAll()
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.requestMatchers("/empleado/**").hasRole("EMPLEADO")
						.requestMatchers("/campo/**").hasRole("CAMPO")
						.requestMatchers("/oficina/**").hasRole("OFICINA")
						.anyRequest().authenticated())
				.formLogin(form -> form.loginPage("/login").successHandler(successHandler).permitAll())
				.logout(logout -> logout.logoutSuccessUrl("/login?logout"));
		return http.build();
	}
}
