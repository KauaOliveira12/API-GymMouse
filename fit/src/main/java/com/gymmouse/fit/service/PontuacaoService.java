package com.gymmouse.fit.service;

import com.gymmouse.fit.model.RegistroPontuacao;
import com.gymmouse.fit.model.Usuario;
import com.gymmouse.fit.repository.RegistroPontuacaoRepository;
import com.gymmouse.fit.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PontuacaoService {

    private final UsuarioRepository usuarioRepository;
    private final RegistroPontuacaoRepository registroPontuacaoRepository;

    public PontuacaoService(UsuarioRepository usuarioRepository, RegistroPontuacaoRepository registroPontuacaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.registroPontuacaoRepository = registroPontuacaoRepository;
    }

    /**
     * Registra pontos com data/hora para uso no ranking por grupo; atualiza {@link Usuario#getPontosTotais()}.
     */
    @Transactional
    public boolean adicionarPontos(Long usuarioId, int quantidade) {
        if (quantidade <= 0) {
            return false;
        }
        return usuarioRepository.findById(usuarioId).map(usuario -> {
            usuario.setPontosTotais(usuario.getPontosTotais() + quantidade);
            usuarioRepository.save(usuario);
            RegistroPontuacao registro = new RegistroPontuacao();
            registro.setUsuario(usuario);
            registro.setQuantidade(quantidade);
            registro.setDataRegistro(LocalDateTime.now());
            registroPontuacaoRepository.save(registro);
            return true;
        }).orElse(false);
    }
}
