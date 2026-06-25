package com.streaks.fit.service;

import com.streaks.fit.dto.AtualizarGrupoRequest;
import com.streaks.fit.dto.CriarGrupoRequest;
import com.streaks.fit.model.Grupo;
import com.streaks.fit.model.Usuario;
import com.streaks.fit.model.UsuarioGrupo;
import com.streaks.fit.repository.GrupoRepository;
import com.streaks.fit.repository.UsuarioGrupoRepository;
import com.streaks.fit.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class GrupoGestaoService {

    private static final String CARACTERES_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TAMANHO_CODIGO = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioGrupoRepository usuarioGrupoRepository;

    public GrupoGestaoService(
            GrupoRepository grupoRepository,
            UsuarioRepository usuarioRepository,
            UsuarioGrupoRepository usuarioGrupoRepository
    ) {
        this.grupoRepository = grupoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioGrupoRepository = usuarioGrupoRepository;
    }

    public enum CriarResultado {
        OK,
        DADOS_INVALIDOS,
        CRIADOR_NAO_ENCONTRADO
    }

    public static final class CriarStatus {
        private final CriarResultado resultado;
        private final Grupo grupo;

        public CriarStatus(CriarResultado resultado, Grupo grupo) {
            this.resultado = resultado;
            this.grupo = grupo;
        }

        public CriarResultado getResultado() {
            return resultado;
        }

        public Grupo getGrupo() {
            return grupo;
        }
    }

    @Transactional
    public CriarStatus criar(CriarGrupoRequest req) {
        if (req == null
                || req.getCriadorId() == null
                || req.getNome() == null
                || req.getNome().isBlank()) {
            return new CriarStatus(CriarResultado.DADOS_INVALIDOS, null);
        }
        Optional<Usuario> criadorOpt = usuarioRepository.findById(req.getCriadorId());
        if (criadorOpt.isEmpty()) {
            return new CriarStatus(CriarResultado.CRIADOR_NAO_ENCONTRADO, null);
        }
        Usuario criador = criadorOpt.get();
        Grupo g = new Grupo();
        g.setNome(req.getNome().trim());
        g.setDescricao(req.getDescricao() != null ? req.getDescricao().trim() : null);
        g.setCriador(criador);
        g.setCodigoAcesso(gerarCodigoAcessoUnico());
        if (!aplicarConfiguracaoInicial(g, req.getImagemCapa(), req.getPontosPorCheckin(),
                req.getDiasSequenciaParaBonus(), req.getMultiplicadorSequencia())) {
            return new CriarStatus(CriarResultado.DADOS_INVALIDOS, null);
        }
        Grupo salvo = grupoRepository.save(g);
        vincularMembro(criador, salvo);
        salvo.setTotalMembros(usuarioGrupoRepository.countByGrupoId(salvo.getId()));
        return new CriarStatus(CriarResultado.OK, salvo);
    }

    public enum AtualizarResultado {
        OK,
        DADOS_INVALIDOS,
        GRUPO_NAO_ENCONTRADO,
        NAO_AUTORIZADO
    }

    public static final class AtualizarStatus {
        private final AtualizarResultado resultado;
        private final Grupo grupo;

        public AtualizarStatus(AtualizarResultado resultado, Grupo grupo) {
            this.resultado = resultado;
            this.grupo = grupo;
        }

        public AtualizarResultado getResultado() {
            return resultado;
        }

        public Grupo getGrupo() {
            return grupo;
        }
    }

    @Transactional
    public AtualizarStatus atualizar(Long grupoId, AtualizarGrupoRequest req) {
        if (req == null || req.getUsuarioId() == null) {
            return new AtualizarStatus(AtualizarResultado.DADOS_INVALIDOS, null);
        }
        Optional<Grupo> opt = grupoRepository.findById(grupoId);
        if (opt.isEmpty()) {
            return new AtualizarStatus(AtualizarResultado.GRUPO_NAO_ENCONTRADO, null);
        }
        Grupo g = opt.get();
        if (!g.getCriador().getId().equals(req.getUsuarioId())) {
            return new AtualizarStatus(AtualizarResultado.NAO_AUTORIZADO, null);
        }
        if (req.getNome() != null && !req.getNome().isBlank()) {
            g.setNome(req.getNome().trim());
        }
        if (req.getDescricao() != null) {
            g.setDescricao(req.getDescricao().isBlank() ? null : req.getDescricao().trim());
        }
        if (req.getImagemCapa() != null) {
            g.setImagemCapa(req.getImagemCapa().isBlank() ? null : req.getImagemCapa().trim());
        }
        if (!aplicarRegrasPontuacao(g, req.getPontosPorCheckin(), req.getDiasSequenciaParaBonus(),
                req.getMultiplicadorSequencia())) {
            return new AtualizarStatus(AtualizarResultado.DADOS_INVALIDOS, null);
        }
        Grupo salvo = grupoRepository.save(g);
        salvo.setTotalMembros(usuarioGrupoRepository.countByGrupoId(salvo.getId()));
        return new AtualizarStatus(AtualizarResultado.OK, salvo);
    }

    public enum DeletarResultado {
        OK,
        GRUPO_NAO_ENCONTRADO,
        NAO_AUTORIZADO
    }

    @Transactional
    public DeletarResultado deletar(Long grupoId, Long usuarioId) {
        if (usuarioId == null) {
            return DeletarResultado.NAO_AUTORIZADO;
        }
        Optional<Grupo> opt = grupoRepository.findById(grupoId);
        if (opt.isEmpty()) {
            return DeletarResultado.GRUPO_NAO_ENCONTRADO;
        }
        Grupo g = opt.get();
        if (!g.getCriador().getId().equals(usuarioId)) {
            return DeletarResultado.NAO_AUTORIZADO;
        }
        usuarioGrupoRepository.deleteByGrupoId(grupoId);
        grupoRepository.delete(g);
        return DeletarResultado.OK;
    }

    private void vincularMembro(Usuario usuario, Grupo grupo) {
        if (usuarioGrupoRepository.existsByUsuarioIdAndGrupoId(usuario.getId(), grupo.getId())) {
            return;
        }
        UsuarioGrupo ug = new UsuarioGrupo();
        ug.setUsuario(usuario);
        ug.setGrupo(grupo);
        ug.setDataEntrada(LocalDateTime.now());
        usuarioGrupoRepository.save(ug);
    }

    private String gerarCodigoAcessoUnico() {
        String codigo;
        int tentativas = 0;
        do {
            StringBuilder sb = new StringBuilder(TAMANHO_CODIGO);
            for (int i = 0; i < TAMANHO_CODIGO; i++) {
                sb.append(CARACTERES_CODIGO.charAt(RANDOM.nextInt(CARACTERES_CODIGO.length())));
            }
            codigo = sb.toString();
            tentativas++;
        } while (grupoRepository.existsByCodigoAcesso(codigo) && tentativas < 100);
        if (grupoRepository.existsByCodigoAcesso(codigo)) {
            codigo = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, TAMANHO_CODIGO).toUpperCase();
        }
        return codigo;
    }

    private boolean aplicarConfiguracaoInicial(
            Grupo grupo,
            String imagemCapa,
            Integer pontosPorCheckin,
            Integer diasSequenciaParaBonus,
            Double multiplicadorSequencia
    ) {
        grupo.setPontosPorCheckin(1);
        if (imagemCapa != null && !imagemCapa.isBlank()) {
            grupo.setImagemCapa(imagemCapa.trim());
        }
        return aplicarRegrasPontuacao(grupo, pontosPorCheckin, diasSequenciaParaBonus, multiplicadorSequencia);
    }

    private boolean aplicarRegrasPontuacao(
            Grupo grupo,
            Integer pontosPorCheckin,
            Integer diasSequenciaParaBonus,
            Double multiplicadorSequencia
    ) {
        if (pontosPorCheckin != null) {
            if (pontosPorCheckin <= 0) {
                return false;
            }
            grupo.setPontosPorCheckin(pontosPorCheckin);
        }
        if (diasSequenciaParaBonus != null) {
            if (diasSequenciaParaBonus <= 0) {
                grupo.setDiasSequenciaParaBonus(null);
                grupo.setMultiplicadorSequencia(null);
            } else {
                grupo.setDiasSequenciaParaBonus(diasSequenciaParaBonus);
            }
        }
        if (multiplicadorSequencia != null) {
            if (multiplicadorSequencia <= 0) {
                grupo.setMultiplicadorSequencia(null);
            } else {
                grupo.setMultiplicadorSequencia(multiplicadorSequencia);
            }
        }
        if (grupo.getDiasSequenciaParaBonus() != null
                && (grupo.getMultiplicadorSequencia() == null || grupo.getMultiplicadorSequencia() <= 0)) {
            return false;
        }
        if (grupo.getMultiplicadorSequencia() != null
                && (grupo.getDiasSequenciaParaBonus() == null || grupo.getDiasSequenciaParaBonus() <= 0)) {
            return false;
        }
        return true;
    }
}
