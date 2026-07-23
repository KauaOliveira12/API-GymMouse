package com.streaks.fit.service;

import com.streaks.fit.model.Usuario;
import com.streaks.fit.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class RecuperacaoSenhaService {

    private static final int MINUTOS_VALIDADE = 15;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    public ResultadoSolicitacao solicitarCodigo(String emailBruto) {
        String email = normalizarEmail(emailBruto);
        if (email.isBlank()) {
            return ResultadoSolicitacao.DADOS_INVALIDOS;
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email);
        if (usuario == null) {
            // Não revela se o e-mail existe ou não.
            return ResultadoSolicitacao.OK;
        }

        String codigo = gerarCodigo();
        usuario.setCodigoRecuperacao(codigo);
        usuario.setCodigoRecuperacaoExpira(LocalDateTime.now().plusMinutes(MINUTOS_VALIDADE));
        usuarioRepository.save(usuario);

        boolean enviado = emailService.enviarCodigoRecuperacao(usuario.getEmail(), codigo);
        if (!enviado) {
            return ResultadoSolicitacao.FALHA_EMAIL;
        }
        return ResultadoSolicitacao.OK;
    }

    public ResultadoRedefinicao redefinirSenha(String emailBruto, String codigoBruto, String novaSenha) {
        String email = normalizarEmail(emailBruto);
        String codigo = codigoBruto == null ? "" : codigoBruto.trim();
        String senha = novaSenha == null ? "" : novaSenha;

        if (email.isBlank() || codigo.isBlank() || senha.isBlank()) {
            return ResultadoRedefinicao.DADOS_INVALIDOS;
        }
        if (senha.length() < 4) {
            return ResultadoRedefinicao.SENHA_CURTA;
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email);
        if (usuario == null
                || usuario.getCodigoRecuperacao() == null
                || usuario.getCodigoRecuperacaoExpira() == null) {
            return ResultadoRedefinicao.CODIGO_INVALIDO;
        }

        if (LocalDateTime.now().isAfter(usuario.getCodigoRecuperacaoExpira())) {
            limparCodigo(usuario);
            return ResultadoRedefinicao.CODIGO_EXPIRADO;
        }

        if (!usuario.getCodigoRecuperacao().equals(codigo)) {
            return ResultadoRedefinicao.CODIGO_INVALIDO;
        }

        usuario.setSenha(senha);
        limparCodigo(usuario);
        return ResultadoRedefinicao.OK;
    }

    private void limparCodigo(Usuario usuario) {
        usuario.setCodigoRecuperacao(null);
        usuario.setCodigoRecuperacaoExpira(null);
        usuarioRepository.save(usuario);
    }

    private String gerarCodigo() {
        int valor = RANDOM.nextInt(1_000_000);
        return String.format("%06d", valor);
    }

    private String normalizarEmail(String email) {
        return email == null ? "" : email.trim();
    }

    public enum ResultadoSolicitacao {
        OK,
        DADOS_INVALIDOS,
        FALHA_EMAIL
    }

    public enum ResultadoRedefinicao {
        OK,
        DADOS_INVALIDOS,
        SENHA_CURTA,
        CODIGO_INVALIDO,
        CODIGO_EXPIRADO
    }
}
