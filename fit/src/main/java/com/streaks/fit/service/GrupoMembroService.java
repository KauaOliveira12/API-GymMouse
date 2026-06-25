package com.streaks.fit.service;

import com.streaks.fit.dto.EntradaGrupoResponse;
import com.streaks.fit.dto.EntrarGrupoRequest;
import com.streaks.fit.dto.RankingPosicao;
import com.streaks.fit.model.Grupo;
import com.streaks.fit.model.Usuario;
import com.streaks.fit.model.UsuarioGrupo;
import com.streaks.fit.repository.GrupoRepository;
import com.streaks.fit.repository.UsuarioGrupoRepository;
import com.streaks.fit.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GrupoMembroService {

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioGrupoRepository usuarioGrupoRepository;

    public GrupoMembroService(
            GrupoRepository grupoRepository,
            UsuarioRepository usuarioRepository,
            UsuarioGrupoRepository usuarioGrupoRepository
    ) {
        this.grupoRepository = grupoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioGrupoRepository = usuarioGrupoRepository;
    }

    public enum EntradaResultado {
        DADOS_INVALIDOS,
        GRUPO_NAO_ENCONTRADO,
        USUARIO_NAO_ENCONTRADO,
        JA_MEMBRO,
        OK
    }

    public static final class EntradaStatus {
        private final EntradaResultado resultado;
        private final EntradaGrupoResponse response;

        public EntradaStatus(EntradaResultado resultado, EntradaGrupoResponse response) {
            this.resultado = resultado;
            this.response = response;
        }

        public EntradaResultado getResultado() {
            return resultado;
        }

        public EntradaGrupoResponse getResponse() {
            return response;
        }
    }

    @Transactional
    public EntradaStatus entrar(EntrarGrupoRequest request) {
        if (request.getCodigoAcesso() == null || request.getCodigoAcesso().isBlank() || request.getUsuarioId() == null) {
            return new EntradaStatus(EntradaResultado.DADOS_INVALIDOS, null);
        }
        String codigo = request.getCodigoAcesso().trim();
        Optional<Grupo> grupoOpt = grupoRepository.findByCodigoAcesso(codigo);
        if (grupoOpt.isEmpty()) {
            return new EntradaStatus(EntradaResultado.GRUPO_NAO_ENCONTRADO, null);
        }
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(request.getUsuarioId());
        if (usuarioOpt.isEmpty()) {
            return new EntradaStatus(EntradaResultado.USUARIO_NAO_ENCONTRADO, null);
        }
        Grupo grupo = grupoOpt.get();
        Usuario usuario = usuarioOpt.get();
        if (usuarioGrupoRepository.existsByUsuarioIdAndGrupoId(usuario.getId(), grupo.getId())) {
            return new EntradaStatus(EntradaResultado.JA_MEMBRO, null);
        }
        UsuarioGrupo membro = new UsuarioGrupo();
        membro.setUsuario(usuario);
        membro.setGrupo(grupo);
        membro.setDataEntrada(LocalDateTime.now());
        UsuarioGrupo salvo = usuarioGrupoRepository.save(membro);
        EntradaGrupoResponse dto = new EntradaGrupoResponse(
                salvo.getId(),
                grupo.getId(),
                grupo.getNome(),
                usuario.getId(),
                salvo.getDataEntrada()
        );
        return new EntradaStatus(EntradaResultado.OK, dto);
    }

    public enum SairResultado {
        DADOS_INVALIDOS,
        GRUPO_NAO_ENCONTRADO,
        NAO_MEMBRO,
        OK
    }

    @Transactional
    public SairResultado sair(Long usuarioId, Long grupoId) {
        if (usuarioId == null || grupoId == null) {
            return SairResultado.DADOS_INVALIDOS;
        }
        if (!grupoRepository.existsById(grupoId)) {
            return SairResultado.GRUPO_NAO_ENCONTRADO;
        }
        if (!usuarioGrupoRepository.existsByUsuarioIdAndGrupoId(usuarioId, grupoId)) {
            return SairResultado.NAO_MEMBRO;
        }
        usuarioGrupoRepository.deleteByUsuarioIdAndGrupoId(usuarioId, grupoId);
        return SairResultado.OK;
    }

    /**
     * Grupos em que o usuário tem vínculo em {@code usuario_grupo} (criador entra na criação; demais via {@code /entrar}).
     */
    public List<Grupo> listarGruposDoUsuario(Long usuarioId) {
        if (usuarioId == null || !usuarioRepository.existsById(usuarioId)) {
            return List.of();
        }
        List<Grupo> grupos = usuarioGrupoRepository.findGruposByUsuarioId(usuarioId);
        for (Grupo g : grupos) {
            popularTotalMembros(g);
        }
        return grupos;
    }

    public void popularTotalMembros(Grupo grupo) {
        if (grupo == null || grupo.getId() == null) return;
        grupo.setTotalMembros(usuarioGrupoRepository.countByGrupoId(grupo.getId()));
    }

    public List<RankingPosicao> rankingPorGrupo(Long grupoId) {
        if (!grupoRepository.existsById(grupoId)) {
            return null;
        }
        List<Object[]> rows = usuarioGrupoRepository.findRankingNativo(grupoId);
        List<RankingPosicao> ranking = new ArrayList<>();
        int posicao = 1;
        for (Object[] row : rows) {
            Long usuarioId = ((Number) row[0]).longValue();
            String nome = (String) row[1];
            long pts = toLong(row[2]);
            ranking.add(new RankingPosicao(posicao++, usuarioId, nome, pts));
        }
        return ranking;
    }

    private static long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        if (value instanceof BigDecimal bd) {
            return bd.longValue();
        }
        return Long.parseLong(value.toString());
    }
}
