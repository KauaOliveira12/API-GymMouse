package com.streaks.fit.dto;

/**
 * Corpo mínimo para criar grupo (ex.: app mobile): use
 * {@code POST /api/usuarios/{id}/grupos} ou {@code POST /api/grupos} com header {@code X-Usuario-Id}
 * e corpo só com nome e descrição.
 */
public class CriarGrupoCorpoLeve {

    private String nome;
    private String descricao;
    private String imagemCapa;
    private Integer pontosPorCheckin;
    private Integer diasSequenciaParaBonus;
    private Double multiplicadorSequencia;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getImagemCapa() {
        return imagemCapa;
    }

    public void setImagemCapa(String imagemCapa) {
        this.imagemCapa = imagemCapa;
    }

    public Integer getPontosPorCheckin() {
        return pontosPorCheckin;
    }

    public void setPontosPorCheckin(Integer pontosPorCheckin) {
        this.pontosPorCheckin = pontosPorCheckin;
    }

    public Integer getDiasSequenciaParaBonus() {
        return diasSequenciaParaBonus;
    }

    public void setDiasSequenciaParaBonus(Integer diasSequenciaParaBonus) {
        this.diasSequenciaParaBonus = diasSequenciaParaBonus;
    }

    public Double getMultiplicadorSequencia() {
        return multiplicadorSequencia;
    }

    public void setMultiplicadorSequencia(Double multiplicadorSequencia) {
        this.multiplicadorSequencia = multiplicadorSequencia;
    }
}
