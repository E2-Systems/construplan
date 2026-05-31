package com.construplan.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.construplan.model.entity.Usuario;
import com.construplan.repository.UsuarioRepository;

@Service
public class UsuarioDetailsService implements UserDetailsService {
	  @Autowired
	    private UsuarioRepository usuarioRepository;

	    @Override
	    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	        Usuario usuario = usuarioRepository.findByUsername(username)
	                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

	        if (!usuario.isActivo())
	            throw new DisabledException("El usuario está inactivo");

	        return new org.springframework.security.core.userdetails.User(
	                usuario.getUsername(),
	                usuario.getPassword(),
	                List.of(new SimpleGrantedAuthority(usuario.getRol().getAuthority()))
	        );
	    }
}
