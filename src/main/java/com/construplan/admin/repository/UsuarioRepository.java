package com.construplan.admin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.construplan.admin.model.entity.Usuario;
import com.construplan.model.entity.Rol;

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
	    
	    Optional<Usuario> findByEmpleado_IdEmpleado(Integer idEmpleado);
	    
	 // Contar usuarios por rol
	    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol = :rol")
	    long countByRol(@Param("rol") Rol rol);
	   
	
}
