package com.gymmouse.fit.controller;

import com.gymmouse.fit.dto.CheckinAcaoRequest;
import com.gymmouse.fit.dto.CheckinResponse;
import com.gymmouse.fit.dto.ComentarioResponse;
import com.gymmouse.fit.dto.CriarCheckinRequest;
import com.gymmouse.fit.service.CheckinService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class CheckinController {

    private final CheckinService checkinService;

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @PostMapping("/api/checkins")
    public ResponseEntity<CheckinResponse> criar(@RequestBody(required = false) CriarCheckinRequest body) {
        CheckinService.CriarStatus status = checkinService.criar(body);
        return switch (status.getResultado()) {
            case OK -> ResponseEntity.ok(status.getResponse());
            case DADOS_INVALIDOS -> ResponseEntity.badRequest().build();
            case USUARIO_NAO_ENCONTRADO -> ResponseEntity.status(404).build();
            case GRUPO_NAO_ENCONTRADO -> ResponseEntity.status(404).build();
            case NAO_MEMBRO -> ResponseEntity.status(403).build();
        };
    }

    @GetMapping("/api/grupos/{grupoId}/checkins")
    public ResponseEntity<List<CheckinResponse>> listarPorGrupo(
            @PathVariable Long grupoId,
            @RequestParam(required = false) Long usuarioId
    ) {
        List<CheckinResponse> checkins = checkinService.listarPorGrupo(grupoId, usuarioId);
        if (checkins == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(checkins);
    }

    @PostMapping("/api/checkins/{checkinId}/curtidas")
    public ResponseEntity<CheckinResponse> alternarCurtida(
            @PathVariable Long checkinId,
            @RequestBody(required = false) CheckinAcaoRequest body
    ) {
        if (body == null || body.getUsuarioId() == null) {
            return ResponseEntity.badRequest().build();
        }
        CheckinResponse response = checkinService.alternarCurtida(checkinId, body.getUsuarioId());
        if (response == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/checkins/{checkinId}/comentarios")
    public ResponseEntity<List<ComentarioResponse>> listarComentarios(@PathVariable Long checkinId) {
        List<ComentarioResponse> comentarios = checkinService.listarComentarios(checkinId);
        if (comentarios == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(comentarios);
    }

    @PostMapping("/api/checkins/{checkinId}/comentarios")
    public ResponseEntity<ComentarioResponse> comentar(
            @PathVariable Long checkinId,
            @RequestBody(required = false) CheckinAcaoRequest body
    ) {
        if (body == null || body.getUsuarioId() == null || body.getTexto() == null || body.getTexto().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        ComentarioResponse comentario = checkinService.comentar(checkinId, body.getUsuarioId(), body.getTexto());
        if (comentario == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(comentario);
    }

    @PostMapping("/api/checkins/{checkinId}/comentarios/{comentarioId}/respostas")
    public ResponseEntity<ComentarioResponse> responderComentario(
            @PathVariable Long checkinId,
            @PathVariable Long comentarioId,
            @RequestBody(required = false) CheckinAcaoRequest body
    ) {
        if (body == null || body.getUsuarioId() == null || body.getTexto() == null || body.getTexto().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        ComentarioResponse comentario = checkinService.comentar(checkinId, body.getUsuarioId(), body.getTexto(), comentarioId);
        if (comentario == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(comentario);
    }

    @GetMapping("/api/checkins/{checkinId}")
    public ResponseEntity<CheckinResponse> buscar(
            @PathVariable Long checkinId,
            @RequestParam(required = false) Long usuarioId
    ) {
        CheckinResponse response = checkinService.buscarResponse(checkinId, usuarioId);
        if (response == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(response);
    }
}
