package com.construplan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.construplan.model.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
	Usuario findByUsername(String username);
}
