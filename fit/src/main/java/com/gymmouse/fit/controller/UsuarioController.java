package com.gymmouse.fit.controller; // Ajuste para o seu pacote

import com.gymmouse.fit.model.Usuario; // Ajuste para o seu pacote
import com.gymmouse.fit.repository.UsuarioRepository; // Ajuste para o seu pacote
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    // Rota para Cadastrar Usuário
    @PostMapping
    public ResponseEntity<Usuario> cadastrarUsuario(@RequestBody Usuario novoUsuario) {
        // Salva no banco de dados e já retorna o usuário com o ID gerado!
        Usuario usuarioSalvo = repository.save(novoUsuario);
        return ResponseEntity.ok(usuarioSalvo);
    }

    @PostMapping("/login")
    public ResponseEntity<Usuario> fazerLogin(@RequestBody Usuario dadosLogin) {
        // Vai no banco e procura se existe alguém com esse email
        Usuario usuarioEncontrado = repository.findByEmail(dadosLogin.getEmail());

        // Se encontrou o usuário E a senha que ele digitou for igual a do banco...
        if (usuarioEncontrado != null && usuarioEncontrado.getSenha().equals(dadosLogin.getSenha())) {
            return ResponseEntity.ok(usuarioEncontrado); // Login Liberado! (Status 200)
        }

        // Se errou a senha ou não existe o e-mail, devolve erro 401 (Não autorizado)
        return ResponseEntity.status(401).build();
    }
}