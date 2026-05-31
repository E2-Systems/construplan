package com.construplan.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.construplan.model.entity.Usuario;
import com.construplan.repository.UsuarioRepository;

@Service
public class UsuarioService {
	 @Autowired
	    private UsuarioRepository usuarioRepository;
	 @Autowired
	    private PasswordEncoder passwordEncoder;
	 
    //Usuario autenticar(String username, String password); --Lo maneja Spring Security vía UserDetailsService
	 public Usuario obtenerPorId(int idUsuario) {
	        if (idUsuario <= 0)
	            throw new IllegalArgumentException("ID de usuario inválido");

	        return usuarioRepository.findById(idUsuario)
	                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
	    }
	 
	 public Usuario obtenerPorUsername(String username) {
	        if (username == null || username.trim().isEmpty())
	            throw new IllegalArgumentException("El usuario no puede estar vacío");

	        return usuarioRepository.findByUsername(username.trim())
	                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
	    }
	 
	 public List<Usuario> listarTodos() {
	        return usuarioRepository.findAll();
	    }
	 
	 public List<Usuario> listarActivos() {
	        return usuarioRepository.findByActivoTrue();
	    }
	 
	 public boolean registrarUsuario(Usuario usuario) {
	        if (usuario == null)
	            throw new IllegalArgumentException("El usuario no puede ser nulo");
	        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty())
	            throw new IllegalArgumentException("El nombre de usuario es requerido");
	        if (usuario.getUsername().length() < 4)
	            throw new IllegalArgumentException("El usuario debe tener al menos 4 caracteres");
	        if (usuario.getPassword() == null || usuario.getPassword().length() < 6)
	            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
	        if (usuario.getRol() == null)
	            throw new IllegalArgumentException("El rol es requerido");
	        if (usuarioRepository.existsByUsername(usuario.getUsername()))
	            throw new IllegalStateException("El nombre de usuario ya existe");

	        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
	        usuarioRepository.save(usuario);
	        return true;
	    }
	 
	 public boolean actualizarUsuario(Usuario usuario) {
	        if (usuario == null || usuario.getId() <= 0)
	            throw new IllegalArgumentException("Usuario inválido");

	        usuarioRepository.findById(usuario.getId())
	                .orElseThrow(() -> new IllegalStateException("El usuario no existe"));

	        usuarioRepository.save(usuario);
	        return true;
	    }
	 
	 @Transactional
	 public boolean cambiarPassword(int idUsuario, String oldPassword, String newPassword) {
	     if (idUsuario <= 0)
	         throw new IllegalArgumentException("ID de usuario inválido");
	     if (newPassword == null || newPassword.length() < 6)
	         throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres");

	     Usuario usuario = usuarioRepository.findById(idUsuario)
	             .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

	     if (!passwordEncoder.matches(oldPassword, usuario.getPassword()))
	         throw new IllegalStateException("La contraseña actual es incorrecta");

	     usuario.setPassword(passwordEncoder.encode(newPassword));
	     usuarioRepository.save(usuario);
	     return true;
	 }
	 
	 @Transactional
	 public boolean cambiarEstado(int idUsuario, boolean activo) {
	     if (idUsuario <= 0)
	         throw new IllegalArgumentException("ID de usuario inválido");

	     Usuario usuario = usuarioRepository.findById(idUsuario)
	             .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

	     usuario.setActivo(activo);
	     usuarioRepository.save(usuario);
	     return true;
	 }
	 
		 
    //boolean validarCredenciales(String username, String password); --Innecesario, Spring Security lo hace
  

}
