package com.streaks.fit.controller;

import com.streaks.fit.dto.PreferenciasNotificacaoRequest;
import com.streaks.fit.dto.PreferenciasNotificacaoResponse;
import com.streaks.fit.dto.RegistrarPushTokenRequest;
import com.streaks.fit.service.NotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/usuarios")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @PutMapping("/{id}/push-token")
    public ResponseEntity<Void> registrarPushToken(
            @PathVariable Long id,
            @RequestBody(required = false) RegistrarPushTokenRequest body
    ) {
        if (!notificacaoService.registrarToken(id, body)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/push-token")
    public ResponseEntity<Void> removerPushToken(
            @PathVariable Long id,
            @RequestBody(required = false) RegistrarPushTokenRequest body
    ) {
        String token = body == null ? null : body.getToken();
        if (!notificacaoService.removerToken(id, token)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/notificacoes")
    public ResponseEntity<PreferenciasNotificacaoResponse> buscarPreferencias(@PathVariable Long id) {
        PreferenciasNotificacaoResponse prefs = notificacaoService.buscarPreferencias(id);
        if (prefs == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(prefs);
    }

    @PutMapping("/{id}/notificacoes")
    public ResponseEntity<PreferenciasNotificacaoResponse> atualizarPreferencias(
            @PathVariable Long id,
            @RequestBody(required = false) PreferenciasNotificacaoRequest body
    ) {
        PreferenciasNotificacaoResponse prefs = notificacaoService.atualizarPreferencias(id, body);
        if (prefs == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(prefs);
    }
}
