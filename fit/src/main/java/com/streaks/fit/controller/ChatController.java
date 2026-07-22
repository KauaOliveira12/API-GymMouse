package com.streaks.fit.controller;

import com.streaks.fit.dto.CriarMensagemRequest;
import com.streaks.fit.model.Grupo;
import com.streaks.fit.model.Mensagem;
import com.streaks.fit.model.Usuario;
import com.streaks.fit.repository.GrupoRepository;
import com.streaks.fit.repository.MensagemRepository;
import com.streaks.fit.repository.UsuarioRepository;
import com.streaks.fit.service.NotificacaoService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final MensagemRepository mensagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final GrupoRepository grupoRepository;
    private final NotificacaoService notificacaoService;

    public ChatController(
            MensagemRepository mensagemRepository,
            UsuarioRepository usuarioRepository,
            GrupoRepository grupoRepository,
            NotificacaoService notificacaoService
    ) {
        this.mensagemRepository = mensagemRepository;
        this.usuarioRepository = usuarioRepository;
        this.grupoRepository = grupoRepository;
        this.notificacaoService = notificacaoService;
    }

    @PostMapping
    public Mensagem enviar(@RequestBody CriarMensagemRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Grupo grupo = grupoRepository.findById(request.grupoId())
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado"));

        Mensagem msg = new Mensagem();
        msg.setConteudo(request.conteudo());
        msg.setDataEnvio(LocalDateTime.now());
        msg.setUsuario(usuario);
        msg.setGrupo(grupo);

        Mensagem salva = mensagemRepository.save(msg);
        String preview = request.conteudo() == null ? "" : request.conteudo().trim();
        if (preview.length() > 80) {
            preview = preview.substring(0, 77) + "...";
        }
        notificacaoService.notificarMembrosDoGrupo(
                grupo.getId(),
                usuario.getId(),
                NotificacaoService.TipoPreferencia.GRUPOS,
                grupo.getNome(),
                usuario.getNome() + ": " + preview,
                Map.of(
                        "grupoId", grupo.getId(),
                        "grupoNome", grupo.getNome() == null ? "" : grupo.getNome(),
                        "tipo", "chat"
                )
        );
        return salva;
    }

    @GetMapping("/{grupoId}")
    public List<Mensagem> listar(@PathVariable Long grupoId) {
        return mensagemRepository.findByGrupoIdOrderByDataEnvioAsc(grupoId);
    }
}
