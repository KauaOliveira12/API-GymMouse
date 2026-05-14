package com.gymmouse.fit.controller; // Ajuste para o seu pacote

import com.gymmouse.fit.dto.AdicionarPontosRequest;
import com.gymmouse.fit.dto.CriarGrupoCorpoLeve;
import com.gymmouse.fit.dto.CriarGrupoRequest;
import com.gymmouse.fit.model.Grupo;
import com.gymmouse.fit.model.Usuario; // Ajuste para o seu pacote
import com.gymmouse.fit.repository.UsuarioRepository; // Ajuste para o seu pacote
import com.gymmouse.fit.service.GrupoGestaoService;
import com.gymmouse.fit.service.PontuacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PontuacaoService pontuacaoService;

    @Autowired
    private GrupoGestaoService grupoGestaoService;

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

    /**
     * Cria um grupo para o usuário logado: envie apenas {@code nome} e {@code descricao}.
     * O {@code usuarioId} da URL é o criador (dono) do grupo. O código de acesso é gerado automaticamente.
     */
    @PostMapping("/{usuarioId}/grupos")
    public ResponseEntity<Grupo> criarGrupo(
            @PathVariable Long usuarioId,
            @RequestBody(required = false) CriarGrupoCorpoLeve corpo
    ) {
        if (corpo == null || corpo.getNome() == null || corpo.getNome().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        CriarGrupoRequest req = new CriarGrupoRequest();
        req.setCriadorId(usuarioId);
        req.setNome(corpo.getNome());
        req.setDescricao(corpo.getDescricao());
        GrupoGestaoService.CriarStatus status = grupoGestaoService.criar(req);
        return switch (status.getResultado()) {
            case OK -> ResponseEntity.ok(status.getGrupo());
            case DADOS_INVALIDOS -> ResponseEntity.badRequest().build();
            case CRIADOR_NAO_ENCONTRADO -> ResponseEntity.status(404).build();
        };
    }

    /**
     * Soma pontos ao usuário e grava histórico datado (usado no ranking por grupo).
     */
    @PostMapping("/{id}/pontos")
    public ResponseEntity<Void> adicionarPontos(@PathVariable Long id, @RequestBody AdicionarPontosRequest body) {
        if (body == null || body.getQuantidade() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        boolean ok = pontuacaoService.adicionarPontos(id, body.getQuantidade());
        if (!ok) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok().build();
    }
}