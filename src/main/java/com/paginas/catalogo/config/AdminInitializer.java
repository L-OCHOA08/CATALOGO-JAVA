package com.paginas.catalogo.config;

import com.paginas.catalogo.model.Usuario;
import com.paginas.catalogo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    @Value("${app.admin.email:}")
    private String email;

    @Value("${app.admin.password:}")
    private String password;

    public AdminInitializer(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (usuarios.count() > 0 || email.isBlank() || password.isBlank()) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setEmail(email);
        admin.setPassword(encoder.encode(password));
        admin.setRol("ADMIN");
        usuarios.save(admin);
    }
}