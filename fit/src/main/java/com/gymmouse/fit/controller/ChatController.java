package com.gymmouse.fit.controller;

import com.gymmouse.fit.dto.CriarMensagemRequest;
import com.gymmouse.fit.model.Grupo;
import com.gymmouse.fit.model.Mensagem;
import com.gymmouse.fit.model.Usuario;
import com.gymmouse.fit.repository.GrupoRepository;
import com.gymmouse.fit.repository.MensagemRepository;
import com.gymmouse.fit.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final MensagemRepository mensagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final GrupoRepository grupoRepository;

    // Injeção de dependência dos repositórios necessários
    public ChatController(MensagemRepository mensagemRepository,
                          UsuarioRepository usuarioRepository,
                          GrupoRepository grupoRepository) {
        this.mensagemRepository = mensagemRepository;
        this.usuarioRepository = usuarioRepository;
        this.grupoRepository = grupoRepository;
    }

    @PostMapping
    public Mensagem enviar(@RequestBody CriarMensagemRequest request) {
        // 1. Busca os objetos reais no banco pelos IDs recebidos
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Grupo grupo = grupoRepository.findById(request.grupoId())
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado"));

        // 2. Cria a mensagem e associa os objetos
        Mensagem msg = new Mensagem();
        msg.setConteudo(request.conteudo());
        msg.setDataEnvio(LocalDateTime.now());
        msg.setUsuario(usuario);
        msg.setGrupo(grupo);

        return mensagemRepository.save(msg);
    }

    @GetMapping("/{grupoId}")
    public List<Mensagem> listar(@PathVariable Long grupoId) {
        return mensagemRepository.findByGrupoIdOrderByDataEnvioAsc(grupoId);
    }
}