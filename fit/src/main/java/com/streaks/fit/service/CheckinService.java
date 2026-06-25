package com.streaks.fit.service;

import com.streaks.fit.dto.CheckinResponse;
import com.streaks.fit.dto.ComentarioResponse;
import com.streaks.fit.dto.CriarCheckinRequest;
import com.streaks.fit.model.Checkin;
import com.streaks.fit.model.CheckinComentario;
import com.streaks.fit.model.CheckinCurtida;
import com.streaks.fit.model.Grupo;
import com.streaks.fit.model.Usuario;
import com.streaks.fit.repository.CheckinComentarioRepository;
import com.streaks.fit.repository.CheckinCurtidaRepository;
import com.streaks.fit.repository.CheckinRepository;
import com.streaks.fit.repository.GrupoRepository;
import com.streaks.fit.repository.UsuarioGrupoRepository;
import com.streaks.fit.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CheckinService {

    private final CheckinRepository checkinRepository;
    private final CheckinCurtidaRepository checkinCurtidaRepository;
    private final CheckinComentarioRepository checkinComentarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final GrupoRepository grupoRepository;
    private final UsuarioGrupoRepository usuarioGrupoRepository;
    private final PontuacaoService pontuacaoService;
    private final PontuacaoGrupoService pontuacaoGrupoService;

    public CheckinService(
            CheckinRepository checkinRepository,
            CheckinCurtidaRepository checkinCurtidaRepository,
            CheckinComentarioRepository checkinComentarioRepository,
            UsuarioRepository usuarioRepository,
            GrupoRepository grupoRepository,
            UsuarioGrupoRepository usuarioGrupoRepository,
            PontuacaoService pontuacaoService,
            PontuacaoGrupoService pontuacaoGrupoService
    ) {
        this.checkinRepository = checkinRepository;
        this.checkinCurtidaRepository = checkinCurtidaRepository;
        this.checkinComentarioRepository = checkinComentarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.grupoRepository = grupoRepository;
        this.usuarioGrupoRepository = usuarioGrupoRepository;
        this.pontuacaoService = pontuacaoService;
        this.pontuacaoGrupoService = pontuacaoGrupoService;
    }

    public enum CriarResultado {
        DADOS_INVALIDOS,
        USUARIO_NAO_ENCONTRADO,
        GRUPO_NAO_ENCONTRADO,
        NAO_MEMBRO,
        OK
    }

    public static final class CriarStatus {
        private final CriarResultado resultado;
        private final CheckinResponse response;

        public CriarStatus(CriarResultado resultado, CheckinResponse response) {
            this.resultado = resultado;
            this.response = response;
        }

        public CriarResultado getResultado() {
            return resultado;
        }

        public CheckinResponse getResponse() {
            return response;
        }
    }

    @Transactional
    public CriarStatus criar(CriarCheckinRequest req) {
        if (req == null || req.getUsuarioId() == null || req.getGrupoId() == null) {
            return new CriarStatus(CriarResultado.DADOS_INVALIDOS, null);
        }
        String titulo = req.getTitulo() == null ? "" : req.getTitulo().trim();
        String imagem = req.getImagem() == null ? "" : req.getImagem().trim();
        if (titulo.isBlank() || imagem.isBlank()) {
            return new CriarStatus(CriarResultado.DADOS_INVALIDOS, null);
        }
        if (!coordenadasValidas(req.getLatitude(), req.getLongitude())) {
            return new CriarStatus(CriarResultado.DADOS_INVALIDOS, null);
        }

        Usuario usuario = usuarioRepository.findById(req.getUsuarioId()).orElse(null);
        if (usuario == null) {
            return new CriarStatus(CriarResultado.USUARIO_NAO_ENCONTRADO, null);
        }
        Grupo grupo = grupoRepository.findById(req.getGrupoId()).orElse(null);
        if (grupo == null) {
            return new CriarStatus(CriarResultado.GRUPO_NAO_ENCONTRADO, null);
        }
        if (!usuarioGrupoRepository.existsByUsuarioIdAndGrupoId(usuario.getId(), grupo.getId())) {
            return new CriarStatus(CriarResultado.NAO_MEMBRO, null);
        }

        Checkin checkin = new Checkin();
        checkin.setUsuario(usuario);
        checkin.setGrupo(grupo);
        checkin.setTitulo(titulo);
        checkin.setDescricao(req.getDescricao() == null ? "" : req.getDescricao().trim());
        checkin.setImagem(imagem);
        checkin.setLatitude(req.getLatitude());
        checkin.setLongitude(req.getLongitude());
        checkin.setLocalizacao(req.getLocalizacao() == null ? "" : req.getLocalizacao().trim());
        int pontos = pontuacaoGrupoService.calcularPontosCheckin(grupo, usuario.getId());
        checkin.setPontos(pontos);
        checkin.setDataCriacao(LocalDateTime.now());

        Checkin salvo = checkinRepository.save(checkin);
        pontuacaoService.adicionarPontos(usuario.getId(), grupo.getId(), salvo.getPontos());

        return new CriarStatus(CriarResultado.OK, new CheckinResponse(salvo));
    }

    @Transactional(readOnly = true)
    public List<CheckinResponse> listarPorGrupo(Long grupoId, Long usuarioId) {
        if (grupoId == null || !grupoRepository.existsById(grupoId)) {
            return null;
        }
        return checkinRepository.findByGrupoIdOrderByDataCriacaoDesc(grupoId)
                .stream()
                .map(checkin -> toResponse(checkin, usuarioId))
                .toList();
    }

    @Transactional
    public CheckinResponse alternarCurtida(Long checkinId, Long usuarioId) {
        Checkin checkin = checkinRepository.findById(checkinId).orElse(null);
        Usuario usuario = usuarioId == null ? null : usuarioRepository.findById(usuarioId).orElse(null);
        if (checkin == null || usuario == null || !podeInteragir(usuario.getId(), checkin)) {
            return null;
        }

        if (checkinCurtidaRepository.existsByCheckinIdAndUsuarioId(checkinId, usuarioId)) {
            checkinCurtidaRepository.deleteByCheckinIdAndUsuarioId(checkinId, usuarioId);
        } else {
            CheckinCurtida curtida = new CheckinCurtida();
            curtida.setCheckin(checkin);
            curtida.setUsuario(usuario);
            curtida.setDataCriacao(LocalDateTime.now());
            checkinCurtidaRepository.save(curtida);
        }

        return toResponse(checkin, usuarioId);
    }

    @Transactional
    public ComentarioResponse comentar(Long checkinId, Long usuarioId, String texto) {
        return comentar(checkinId, usuarioId, texto, null);
    }

    @Transactional
    public ComentarioResponse comentar(Long checkinId, Long usuarioId, String texto, Long comentarioPaiId) {
        Checkin checkin = checkinRepository.findById(checkinId).orElse(null);
        Usuario usuario = usuarioId == null ? null : usuarioRepository.findById(usuarioId).orElse(null);
        String textoLimpo = texto == null ? "" : texto.trim();
        if (checkin == null || usuario == null || textoLimpo.isBlank() || !podeInteragir(usuario.getId(), checkin)) {
            return null;
        }
        CheckinComentario comentarioPai = null;
        if (comentarioPaiId != null) {
            comentarioPai = checkinComentarioRepository.findById(comentarioPaiId).orElse(null);
            if (comentarioPai == null || !comentarioPai.getCheckin().getId().equals(checkin.getId())) {
                return null;
            }
        }

        CheckinComentario comentario = new CheckinComentario();
        comentario.setCheckin(checkin);
        comentario.setUsuario(usuario);
        comentario.setComentarioPai(comentarioPai);
        comentario.setTexto(textoLimpo);
        comentario.setDataCriacao(LocalDateTime.now());
        return new ComentarioResponse(checkinComentarioRepository.save(comentario));
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listarComentarios(Long checkinId) {
        if (checkinId == null || !checkinRepository.existsById(checkinId)) {
            return null;
        }
        List<ComentarioResponse> todos = checkinComentarioRepository.findByCheckinIdOrderByDataCriacaoAsc(checkinId)
                .stream()
                .map(ComentarioResponse::new)
                .toList();
        Map<Long, ComentarioResponse> porId = todos.stream()
                .collect(Collectors.toMap(ComentarioResponse::getId, comentario -> comentario));

        todos.forEach(comentario -> {
            if (comentario.getComentarioPaiId() != null) {
                ComentarioResponse pai = porId.get(comentario.getComentarioPaiId());
                if (pai != null) {
                    pai.getRespostas().add(comentario);
                }
            }
        });

        return todos.stream()
                .filter(comentario -> comentario.getComentarioPaiId() == null)
                .toList();
    }

    @Transactional(readOnly = true)
    public CheckinResponse buscarResponse(Long checkinId, Long usuarioId) {
        return checkinRepository.findById(checkinId)
                .map(checkin -> toResponse(checkin, usuarioId))
                .orElse(null);
    }

    private CheckinResponse toResponse(Checkin checkin, Long usuarioId) {
        long likes = checkinCurtidaRepository.countByCheckinId(checkin.getId());
        long comments = checkinComentarioRepository.countByCheckinId(checkin.getId());
        boolean curtido = usuarioId != null && checkinCurtidaRepository.existsByCheckinIdAndUsuarioId(checkin.getId(), usuarioId);
        return new CheckinResponse(checkin, likes, comments, curtido);
    }

    private boolean coordenadasValidas(Double latitude, Double longitude) {
        if (latitude == null && longitude == null) {
            return true;
        }
        if (latitude == null || longitude == null) {
            return false;
        }
        return latitude >= -90
                && latitude <= 90
                && longitude >= -180
                && longitude <= 180;
    }

    private boolean podeInteragir(Long usuarioId, Checkin checkin) {
        return usuarioGrupoRepository.existsByUsuarioIdAndGrupoId(usuarioId, checkin.getGrupo().getId());
    }
}
