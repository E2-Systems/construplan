package com.construplan.service;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.construplan.model.entity.Usuario;
import com.construplan.repository.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired 
	private UsuarioRepository usuarioRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		Usuario usuario = usuarioRepository.findByUsername(username);
		
		if (usuario == null) {
			throw new UsernameNotFoundException("Usuario no encontrado");
		}

		return new User(
				usuario.getUsername(), 
				usuario.getPassword(),
				List.of(
						new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())
						)
				);
	}

}
