package com.streaks.fit.controller; // Ajuste para o seu pacote

import com.streaks.fit.dto.AdicionarPontosRequest;
import com.streaks.fit.dto.AtualizarUsuarioRequest;
import com.streaks.fit.dto.CriarGrupoCorpoLeve;
import com.streaks.fit.dto.CriarGrupoRequest;
import com.streaks.fit.dto.EsqueciSenhaRequest;
import com.streaks.fit.dto.MensagemResponse;
import com.streaks.fit.dto.RedefinirSenhaRequest;
import com.streaks.fit.model.Grupo;
import com.streaks.fit.model.Usuario; // Ajuste para o seu pacote
import com.streaks.fit.repository.UsuarioRepository; // Ajuste para o seu pacote
import com.streaks.fit.service.GrupoGestaoService;
import com.streaks.fit.service.PontuacaoService;
import com.streaks.fit.service.RecuperacaoSenhaService;
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

    @Autowired
    private RecuperacaoSenhaService recuperacaoSenhaService;

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
     * Gera um código de 6 dígitos e envia por e-mail para redefinir a senha.
     * Resposta genérica para não revelar se o e-mail existe.
     */
    @PostMapping("/esqueci-senha")
    public ResponseEntity<MensagemResponse> esqueciSenha(@RequestBody(required = false) EsqueciSenhaRequest body) {
        if (body == null) {
            return ResponseEntity.badRequest().body(new MensagemResponse("Informe o e-mail."));
        }

        RecuperacaoSenhaService.ResultadoSolicitacao resultado =
                recuperacaoSenhaService.solicitarCodigo(body.getEmail());

        return switch (resultado) {
            case DADOS_INVALIDOS -> ResponseEntity.badRequest()
                    .body(new MensagemResponse("Informe um e-mail válido."));
            case FALHA_EMAIL -> ResponseEntity.status(503)
                    .body(new MensagemResponse(
                            "Não foi possível enviar o e-mail agora. Tente novamente em instantes."
                    ));
            case OK -> ResponseEntity.ok(new MensagemResponse(
                    "Se este e-mail estiver cadastrado, enviaremos um código para redefinir a senha."
            ));
        };
    }

    /**
     * Valida o código recebido por e-mail e grava a nova senha.
     */
    @PostMapping("/redefinir-senha")
    public ResponseEntity<MensagemResponse> redefinirSenha(@RequestBody(required = false) RedefinirSenhaRequest body) {
        if (body == null) {
            return ResponseEntity.badRequest().body(new MensagemResponse("Dados inválidos."));
        }

        RecuperacaoSenhaService.ResultadoRedefinicao resultado = recuperacaoSenhaService.redefinirSenha(
                body.getEmail(),
                body.getCodigo(),
                body.getNovaSenha()
        );

        return switch (resultado) {
            case DADOS_INVALIDOS -> ResponseEntity.badRequest()
                    .body(new MensagemResponse("Preencha e-mail, código e nova senha."));
            case SENHA_CURTA -> ResponseEntity.badRequest()
                    .body(new MensagemResponse("A nova senha deve ter pelo menos 4 caracteres."));
            case CODIGO_INVALIDO -> ResponseEntity.status(400)
                    .body(new MensagemResponse("Código inválido. Solicite um novo código."));
            case CODIGO_EXPIRADO -> ResponseEntity.status(400)
                    .body(new MensagemResponse("Código expirado. Solicite um novo código."));
            case OK -> ResponseEntity.ok(new MensagemResponse("Senha redefinida com sucesso. Faça login."));
        };
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarUsuario(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizarUsuario(
            @PathVariable Long id,
            @RequestBody(required = false) AtualizarUsuarioRequest body
    ) {
        if (body == null) {
            return ResponseEntity.badRequest().build();
        }
        return repository.findById(id).map(usuario -> {
            String nome = body.getNome() == null ? "" : body.getNome().trim();
            String email = body.getEmail() == null ? "" : body.getEmail().trim();
            if (nome.isBlank() || email.isBlank()) {
                return ResponseEntity.badRequest().<Usuario>build();
            }

            Usuario usuarioComEmail = repository.findByEmail(email);
            if (usuarioComEmail != null && !usuarioComEmail.getId().equals(id)) {
                return ResponseEntity.status(409).<Usuario>build();
            }

            usuario.setNome(nome);
            usuario.setEmail(email);
            if (body.getSenha() != null && !body.getSenha().isBlank()) {
                usuario.setSenha(body.getSenha());
            }
            // null = não altera a foto; string vazia = remove; valor = grava
            if (body.getFotoPerfil() != null) {
                String foto = body.getFotoPerfil().trim();
                usuario.setFotoPerfil(foto.isBlank() ? null : foto);
            }
            return ResponseEntity.ok(repository.save(usuario));
        }).orElseGet(() -> ResponseEntity.status(404).build());
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
        req.setImagemCapa(corpo.getImagemCapa());
        req.setPontosPorCheckin(corpo.getPontosPorCheckin());
        req.setDiasSequenciaParaBonus(corpo.getDiasSequenciaParaBonus());
        req.setMultiplicadorSequencia(corpo.getMultiplicadorSequencia());
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
