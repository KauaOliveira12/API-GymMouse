package com.streaks.fit.service;

import com.streaks.fit.dto.PreferenciasNotificacaoRequest;
import com.streaks.fit.dto.PreferenciasNotificacaoResponse;
import com.streaks.fit.dto.RegistrarPushTokenRequest;
import com.streaks.fit.model.DispositivoPush;
import com.streaks.fit.model.PreferenciaNotificacao;
import com.streaks.fit.model.Usuario;
import com.streaks.fit.repository.DispositivoPushRepository;
import com.streaks.fit.repository.PreferenciaNotificacaoRepository;
import com.streaks.fit.repository.UsuarioGrupoRepository;
import com.streaks.fit.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class NotificacaoService {

    public enum TipoPreferencia {
        CHECKINS,
        GRUPOS,
        RANKING
    }

    private final UsuarioRepository usuarioRepository;
    private final DispositivoPushRepository dispositivoPushRepository;
    private final PreferenciaNotificacaoRepository preferenciaNotificacaoRepository;
    private final UsuarioGrupoRepository usuarioGrupoRepository;
    private final ExpoPushService expoPushService;

    public NotificacaoService(
            UsuarioRepository usuarioRepository,
            DispositivoPushRepository dispositivoPushRepository,
            PreferenciaNotificacaoRepository preferenciaNotificacaoRepository,
            UsuarioGrupoRepository usuarioGrupoRepository,
            ExpoPushService expoPushService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.dispositivoPushRepository = dispositivoPushRepository;
        this.preferenciaNotificacaoRepository = preferenciaNotificacaoRepository;
        this.usuarioGrupoRepository = usuarioGrupoRepository;
        this.expoPushService = expoPushService;
    }

    @Transactional
    public boolean registrarToken(Long usuarioId, RegistrarPushTokenRequest request) {
        if (usuarioId == null || request == null || request.getToken() == null || request.getToken().isBlank()) {
            return false;
        }
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            return false;
        }
        String token = request.getToken().trim();
        DispositivoPush dispositivo = dispositivoPushRepository.findByToken(token).orElseGet(DispositivoPush::new);
        dispositivo.setUsuario(usuario);
        dispositivo.setToken(token);
        dispositivo.setPlataforma(request.getPlataforma() == null ? "" : request.getPlataforma().trim());
        dispositivoPushRepository.save(dispositivo);
        garantirPreferencias(usuario);
        return true;
    }

    @Transactional
    public boolean removerToken(Long usuarioId, String token) {
        if (usuarioId == null || token == null || token.isBlank()) {
            return false;
        }
        dispositivoPushRepository.deleteByUsuarioIdAndToken(usuarioId, token.trim());
        return true;
    }

    @Transactional(readOnly = true)
    public PreferenciasNotificacaoResponse buscarPreferencias(Long usuarioId) {
        if (usuarioId == null || !usuarioRepository.existsById(usuarioId)) {
            return null;
        }
        return preferenciaNotificacaoRepository.findByUsuarioId(usuarioId)
                .map(PreferenciasNotificacaoResponse::new)
                .orElseGet(PreferenciasNotificacaoResponse::padrao);
    }

    @Transactional
    public PreferenciasNotificacaoResponse atualizarPreferencias(Long usuarioId, PreferenciasNotificacaoRequest request) {
        if (usuarioId == null || request == null) {
            return null;
        }
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            return null;
        }
        PreferenciaNotificacao pref = garantirPreferencias(usuario);
        if (request.getCheckins() != null) {
            pref.setCheckins(request.getCheckins());
        }
        if (request.getGrupos() != null) {
            pref.setGrupos(request.getGrupos());
        }
        if (request.getRanking() != null) {
            pref.setRanking(request.getRanking());
        }
        if (request.getLembretes() != null) {
            pref.setLembretes(request.getLembretes());
        }
        return new PreferenciasNotificacaoResponse(preferenciaNotificacaoRepository.save(pref));
    }

    @Transactional(readOnly = true)
    public void notificarMembrosDoGrupo(
            Long grupoId,
            Long usuarioOrigemId,
            TipoPreferencia tipo,
            String titulo,
            String corpo,
            Map<String, Object> data
    ) {
        if (grupoId == null) {
            return;
        }
        List<Long> membros = usuarioGrupoRepository.findUsuarioIdsByGrupoId(grupoId);
        if (membros == null || membros.isEmpty()) {
            return;
        }
        Set<Long> destinatarios = membros.stream()
                .filter(Objects::nonNull)
                .filter(id -> usuarioOrigemId == null || !id.equals(usuarioOrigemId))
                .collect(Collectors.toCollection(HashSet::new));
        if (destinatarios.isEmpty()) {
            return;
        }

        Map<String, Object> payload = data == null ? new HashMap<>() : new HashMap<>(data);
        payload.putIfAbsent("grupoId", grupoId);
        payload.putIfAbsent("tipo", tipo.name().toLowerCase());

        Map<Long, PreferenciaNotificacao> prefs = preferenciaNotificacaoRepository.findByUsuarioIdIn(destinatarios)
                .stream()
                .collect(Collectors.toMap(p -> p.getUsuario().getId(), p -> p, (a, b) -> a));

        Predicate<Long> permitido = usuarioId -> {
            PreferenciaNotificacao pref = prefs.get(usuarioId);
            if (pref == null) {
                return true;
            }
            return switch (tipo) {
                case CHECKINS -> pref.isCheckins();
                case GRUPOS -> pref.isGrupos();
                case RANKING -> pref.isRanking();
            };
        };

        Set<Long> filtrados = destinatarios.stream().filter(permitido).collect(Collectors.toSet());
        if (filtrados.isEmpty()) {
            return;
        }

        List<String> tokens = dispositivoPushRepository.findByUsuarioIdIn(filtrados).stream()
                .map(DispositivoPush::getToken)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(t -> !t.isBlank())
                .distinct()
                .toList();
        if (tokens.isEmpty()) {
            return;
        }
        CompletableFuture.runAsync(() -> expoPushService.enviar(tokens, titulo, corpo, payload));
    }

    private PreferenciaNotificacao garantirPreferencias(Usuario usuario) {
        return preferenciaNotificacaoRepository.findByUsuarioId(usuario.getId()).orElseGet(() -> {
            PreferenciaNotificacao nova = new PreferenciaNotificacao();
            nova.setUsuario(usuario);
            return preferenciaNotificacaoRepository.save(nova);
        });
    }
}
