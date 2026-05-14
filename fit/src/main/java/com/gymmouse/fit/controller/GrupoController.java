package com.gymmouse.fit.controller;

import com.gymmouse.fit.dto.AtualizarGrupoRequest;
import com.gymmouse.fit.dto.CriarGrupoCorpoLeve;
import com.gymmouse.fit.dto.CriarGrupoRequest;
import com.gymmouse.fit.dto.EntradaGrupoResponse;
import com.gymmouse.fit.dto.EntrarGrupoRequest;
import com.gymmouse.fit.dto.RankingPosicao;
import com.gymmouse.fit.dto.SairGrupoRequest;
import com.gymmouse.fit.model.Grupo;
import com.gymmouse.fit.repository.GrupoRepository;
import com.gymmouse.fit.service.GrupoGestaoService;
import com.gymmouse.fit.service.GrupoMembroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/grupos")
public class GrupoController {

    @Autowired
    private GrupoRepository repository;

    @Autowired
    private GrupoMembroService grupoMembroService;

    @Autowired
    private GrupoGestaoService grupoGestaoService;

    /**
     * Cria grupo. O criador identifica-se por uma destas formas (nesta ordem):
     * corpo {@link CriarGrupoRequest#criadorId}, query {@code criadorId}, header {@code X-Usuario-Id},
     * ou rota {@code POST /api/usuarios/{id}/grupos} com {@link CriarGrupoCorpoLeve}.
     * No JSON envie apenas {@code nome} e {@code descricao} se usar o header ou o query param.
     * O {@code id} e o {@code codigoAcesso} do grupo são sempre gerados no servidor.
     */
    @PostMapping
    public ResponseEntity<Grupo> criarGrupo(
            @RequestBody(required = false) CriarGrupoRequest body,
            @RequestParam(required = false) Long criadorId,
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioIdHeader
    ) {
        if (body == null) {
            return ResponseEntity.badRequest().build();
        }
        if (body.getCriadorId() == null && criadorId != null) {
            body.setCriadorId(criadorId);
        }
        if (body.getCriadorId() == null) {
            Long fromHeader = parseLongHeader(usuarioIdHeader);
            if (fromHeader != null) {
                body.setCriadorId(fromHeader);
            }
        }
        GrupoGestaoService.CriarStatus status = grupoGestaoService.criar(body);
        return switch (status.getResultado()) {
            case OK -> ResponseEntity.ok(status.getGrupo());
            case DADOS_INVALIDOS -> ResponseEntity.badRequest().build();
            case CRIADOR_NAO_ENCONTRADO -> ResponseEntity.status(404).build();
        };
    }

    @PostMapping("/entrar")
    public ResponseEntity<EntradaGrupoResponse> entrarPorCodigo(@RequestBody EntrarGrupoRequest body) {
        GrupoMembroService.EntradaStatus status = grupoMembroService.entrar(body);
        return switch (status.getResultado()) {
            case OK -> ResponseEntity.ok(status.getResponse());
            case DADOS_INVALIDOS -> ResponseEntity.badRequest().build();
            case USUARIO_NAO_ENCONTRADO -> ResponseEntity.status(404).build();
            case GRUPO_NAO_ENCONTRADO -> ResponseEntity.status(404).build();
            case JA_MEMBRO -> ResponseEntity.status(409).build();
        };
    }

    @PostMapping("/sair")
    public ResponseEntity<Void> sair(@RequestBody(required = false) SairGrupoRequest body) {
        if (body == null) {
            return ResponseEntity.badRequest().build();
        }
        GrupoMembroService.SairResultado r = grupoMembroService.sair(body.getUsuarioId(), body.getGrupoId());
        return switch (r) {
            case OK -> ResponseEntity.noContent().build();
            case DADOS_INVALIDOS -> ResponseEntity.badRequest().build();
            case GRUPO_NAO_ENCONTRADO -> ResponseEntity.status(404).build();
            case NAO_MEMBRO -> ResponseEntity.status(404).build();
        };
    }

    /**
     * Atualiza nome e/ou descrição. Somente o criador do grupo ({@code usuarioId}). O código de acesso não é alterado aqui.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Grupo> atualizarGrupo(@PathVariable Long id, @RequestBody AtualizarGrupoRequest body) {
        GrupoGestaoService.AtualizarStatus status = grupoGestaoService.atualizar(id, body);
        return switch (status.getResultado()) {
            case OK -> ResponseEntity.ok(status.getGrupo());
            case DADOS_INVALIDOS -> ResponseEntity.badRequest().build();
            case GRUPO_NAO_ENCONTRADO -> ResponseEntity.status(404).build();
            case NAO_AUTORIZADO -> ResponseEntity.status(403).build();
        };
    }

    /**
     * Remove o grupo e vínculos de membros. Somente o criador ({@code usuarioId}).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarGrupo(@PathVariable Long id, @RequestParam Long usuarioId) {
        return switch (grupoGestaoService.deletar(id, usuarioId)) {
            case OK -> ResponseEntity.noContent().build();
            case GRUPO_NAO_ENCONTRADO -> ResponseEntity.status(404).build();
            case NAO_AUTORIZADO -> ResponseEntity.status(403).build();
        };
    }

    @GetMapping("/{id}/ranking")
    public ResponseEntity<List<RankingPosicao>> ranking(@PathVariable Long id) {
        List<RankingPosicao> ranking = grupoMembroService.rankingPorGrupo(id);
        if (ranking == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(ranking);
    }

    @GetMapping
    public ResponseEntity<List<Grupo>> listarGrupos(
            @RequestParam(required = false) Boolean doUsuario,
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioIdHeader
    ) {
        if (Boolean.TRUE.equals(doUsuario)) {
            Long usuarioId = parseLongHeader(usuarioIdHeader);
            if (usuarioId == null) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(grupoMembroService.listarGruposDoUsuario(usuarioId));
        }
        List<Grupo> grupos = repository.findAll();
        grupos.forEach(grupoMembroService::popularTotalMembros);
        return ResponseEntity.ok(grupos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Grupo> buscarGrupoPorId(@PathVariable Long id) {
        Optional<Grupo> grupo = repository.findById(id);

        if (grupo.isPresent()) {
            Grupo g = grupo.get();
            grupoMembroService.popularTotalMembros(g);
            return ResponseEntity.ok(g);
        }

        return ResponseEntity.status(404).build();
    }

    private static Long parseLongHeader(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
