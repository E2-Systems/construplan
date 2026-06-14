package com.construplan.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.construplan.admin.model.entity.Usuario;
import com.construplan.admin.repository.UsuarioRepository;
import com.construplan.model.entity.Rol;
import com.construplan.repository.EmpleadoRepository;

@Service
public class UsuarioDetailsService implements UserDetailsService {
	  @Autowired
	    private UsuarioRepository usuarioRepository;
	  
	  @Autowired
	    private EmpleadoRepository empleadoRepository;

	    @Override
	    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	        Usuario usuario = usuarioRepository.findByUsername(username)
	                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

	        if (!usuario.isActivo())
	            throw new DisabledException("El usuario está inactivo");
	        
	      
	        // Si es EMPLEADO debe tener un empleado asociado
	        if (usuario.getRol() == Rol.EMPLEADO) {
	            boolean tieneEmpleado = empleadoRepository.findByUsuario_Id(usuario.getId()).isPresent();
	            if (!tieneEmpleado)
	                throw new DisabledException("No existe empleado asociado al usuario");
	        }

	        
	        return new org.springframework.security.core.userdetails.User(
	                usuario.getUsername(),
	                usuario.getPassword(),
	                List.of(new SimpleGrantedAuthority(usuario.getRol().getAuthority()))
	        );
	    }
}
