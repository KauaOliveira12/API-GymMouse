package com.streaks.fit.service;

import com.streaks.fit.model.Grupo;
import com.streaks.fit.model.RegistroPontuacao;
import com.streaks.fit.model.Usuario;
import com.streaks.fit.repository.GrupoRepository;
import com.streaks.fit.repository.RegistroPontuacaoRepository;
import com.streaks.fit.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PontuacaoService {

    private final UsuarioRepository usuarioRepository;
    private final GrupoRepository grupoRepository;
    private final RegistroPontuacaoRepository registroPontuacaoRepository;

    public PontuacaoService(
            UsuarioRepository usuarioRepository,
            GrupoRepository grupoRepository,
            RegistroPontuacaoRepository registroPontuacaoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.grupoRepository = grupoRepository;
        this.registroPontuacaoRepository = registroPontuacaoRepository;
    }

    /**
     * Registra pontos com data/hora para uso no ranking por grupo; atualiza {@link Usuario#getPontosTotais()}.
     */
    @Transactional
    public boolean adicionarPontos(Long usuarioId, int quantidade) {
        return adicionarPontos(usuarioId, null, quantidade);
    }

    @Transactional
    public boolean adicionarPontos(Long usuarioId, Long grupoId, int quantidade) {
        if (quantidade <= 0) {
            return false;
        }
        Grupo grupo = null;
        if (grupoId != null) {
            grupo = grupoRepository.findById(grupoId).orElse(null);
            if (grupo == null) {
                return false;
            }
        }
        Grupo grupoFinal = grupo;
        return usuarioRepository.findById(usuarioId).map(usuario -> {
            usuario.setPontosTotais(usuario.getPontosTotais() + quantidade);
            usuarioRepository.save(usuario);
            RegistroPontuacao registro = new RegistroPontuacao();
            registro.setUsuario(usuario);
            registro.setGrupo(grupoFinal);
            registro.setQuantidade(quantidade);
            registro.setDataRegistro(LocalDateTime.now());
            registroPontuacaoRepository.save(registro);
            return true;
        }).orElse(false);
    }
}
