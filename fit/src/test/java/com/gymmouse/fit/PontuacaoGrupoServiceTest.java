package com.gymmouse.fit;

import com.gymmouse.fit.model.Checkin;
import com.gymmouse.fit.model.Grupo;
import com.gymmouse.fit.repository.CheckinRepository;
import com.gymmouse.fit.service.PontuacaoGrupoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PontuacaoGrupoServiceTest {

    @Mock
    private CheckinRepository checkinRepository;

    private PontuacaoGrupoService service;

    @BeforeEach
    void setUp() {
        service = new PontuacaoGrupoService(checkinRepository);
    }

    @Test
    void calcularPontosCheckin_usaPontosBaseDoGrupo() {
        Grupo grupo = grupoComRegras(5, null, null);

        assertThat(service.calcularPontosCheckin(grupo, 1L)).isEqualTo(5);
    }

    @Test
    void calcularPontosCheckin_aplicaMultiplicadorNaSequencia() {
        Grupo grupo = grupoComRegras(1, 3, 2.0);
        LocalDate hoje = LocalDate.now();
        when(checkinRepository.findByUsuarioIdAndGrupoId(1L, 10L)).thenReturn(List.of(
                checkinEm(hoje.minusDays(1)),
                checkinEm(hoje.minusDays(2))
        ));

        assertThat(service.calcularPontosCheckin(grupo, 1L)).isEqualTo(2);
    }

    @Test
    void calcularPontosCheckin_semSequenciaSuficiente_naoAplicaBonus() {
        Grupo grupo = grupoComRegras(2, 3, 2.0);
        when(checkinRepository.findByUsuarioIdAndGrupoId(1L, 10L)).thenReturn(List.of());

        assertThat(service.calcularPontosCheckin(grupo, 1L)).isEqualTo(2);
    }

    private static Grupo grupoComRegras(int base, Integer diasBonus, Double multiplicador) {
        Grupo grupo = new Grupo();
        grupo.setId(10L);
        grupo.setPontosPorCheckin(base);
        grupo.setDiasSequenciaParaBonus(diasBonus);
        grupo.setMultiplicadorSequencia(multiplicador);
        return grupo;
    }

    private static Checkin checkinEm(LocalDate dia) {
        Checkin checkin = new Checkin();
        checkin.setDataCriacao(dia.atTime(12, 0));
        return checkin;
    }
}
