package com.streaks.fit.service;

import com.streaks.fit.model.Checkin;
import com.streaks.fit.model.Grupo;
import com.streaks.fit.repository.CheckinRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PontuacaoGrupoService {

    private final CheckinRepository checkinRepository;

    public PontuacaoGrupoService(CheckinRepository checkinRepository) {
        this.checkinRepository = checkinRepository;
    }

    /**
     * Calcula pontos de um check-in conforme as regras definidas pelo criador do grupo.
     */
    @Transactional(readOnly = true)
    public int calcularPontosCheckin(Grupo grupo, Long usuarioId) {
        int base = pontosBase(grupo);
        if (!bonusSequenciaAtivo(grupo)) {
            return base;
        }
        int sequencia = calcularSequenciaDiasConsecutivos(usuarioId, grupo.getId());
        if (sequencia >= grupo.getDiasSequenciaParaBonus()) {
            return Math.max(1, (int) Math.round(base * grupo.getMultiplicadorSequencia()));
        }
        return base;
    }

    private static int pontosBase(Grupo grupo) {
        Integer pontos = grupo.getPontosPorCheckin();
        if (pontos == null || pontos <= 0) {
            return 1;
        }
        return pontos;
    }

    private static boolean bonusSequenciaAtivo(Grupo grupo) {
        Integer dias = grupo.getDiasSequenciaParaBonus();
        Double multiplicador = grupo.getMultiplicadorSequencia();
        return dias != null && dias > 0 && multiplicador != null && multiplicador > 0;
    }

    private int calcularSequenciaDiasConsecutivos(Long usuarioId, Long grupoId) {
        LocalDate hoje = LocalDate.now();
        List<Checkin> checkins = checkinRepository.findByUsuarioIdAndGrupoId(usuarioId, grupoId);
        Set<LocalDate> datas = new HashSet<>();
        for (Checkin checkin : checkins) {
            datas.add(checkin.getDataCriacao().toLocalDate());
        }
        datas.add(hoje);

        int sequencia = 0;
        LocalDate dia = hoje;
        while (datas.contains(dia)) {
            sequencia++;
            dia = dia.minusDays(1);
        }
        return sequencia;
    }
}
