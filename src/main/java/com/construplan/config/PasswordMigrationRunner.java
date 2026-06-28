package com.construplan.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.construplan.admin.model.entity.Usuario;
import com.construplan.admin.repository.UsuarioRepository;



/**
 * Componente de migración única que detecta contraseñas almacenadas en texto plano
 * y las re-encripta con BCrypt al arrancar la aplicación.
 *
 * Los hashes BCrypt siempre comienzan con "$2a$", "$2b$" o "$2y$",
 * por lo que cualquier contraseña que no siga ese patrón se considera texto plano.
 *
 * Una vez que todas las contraseñas estén migradas, este componente puede eliminarse.
 */
@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PasswordMigrationRunner.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        List<Usuario> usersWithPlainPassword = usuarioRepository.findAll().stream()
                .filter(user -> !isBcryptHash(user.getPassword()))
                .toList();

        if (usersWithPlainPassword.isEmpty()) {
            log.info("Migración de contraseñas: todas las contraseñas ya están encriptadas con BCrypt.");
            return;
        }

        log.warn("Migración de contraseñas: se encontraron {} usuarios con contraseña en texto plano. Migrando...",
                usersWithPlainPassword.size());

        for (Usuario user : usersWithPlainPassword) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            usuarioRepository.save(user);
            log.info("Migración de contraseñas: usuario '{}' migrado exitosamente.", user.getUsername());
        }

        log.info("Migración de contraseñas completada. {} usuarios actualizados.", usersWithPlainPassword.size());
    }

    /**
     * Verifica si el valor ya es un hash BCrypt válido.
     * Los hashes BCrypt tienen 60 caracteres y comienzan con "$2a$", "$2b$" o "$2y$".
     */
    private boolean isBcryptHash(String password) {
        if (password == null || password.length() != 60) {
            return false;
        }
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }
}
