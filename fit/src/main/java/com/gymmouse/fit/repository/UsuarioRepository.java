package com.gymmouse.fit.repository; // Ajuste para o seu pacote

import com.gymmouse.fit.model.Usuario; // Ajuste para o seu pacote
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String email);
}