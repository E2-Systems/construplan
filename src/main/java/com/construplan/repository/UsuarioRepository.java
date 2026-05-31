package com.construplan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.construplan.model.entity.Rol;
import com.construplan.model.entity.Usuario;

import jakarta.transaction.Transactional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
	
	  //Usuario buscarPorId(int idUsuario); --Heredado findById()	 
	  //List<Usuario> listarTodos(); --Heredado findAll() 
	    //boolean crear(Usuario usuario); --Heredado save() 
	    //boolean actualizar(Usuario usuario);--Heredado save() 
    
	 Optional<Usuario> findByUsername(String username);
	 Optional<Usuario> findByUsernameAndPassword(String username, String password);
	 
	 List<Usuario> findByActivoTrue();	    
	    List<Usuario> findByRol(Rol rol);
	   
	    
	    List<Usuario> findByActivoTrueOrderByFechaCreacionDesc(); //findUsuariosByEstado
	    List<Usuario> findByActivoFalseOrderByFechaCreacionDesc();
	    
	    boolean existsByUsername(String username);
	    
	 
	    
	    
	   
	
}
