package com.gymmouse.fit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CriarGrupoRequest {

    private String nome;
    private String descricao;
    private Long criadorId;
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

    public Long getCriadorId() {
        return criadorId;
    }

    public void setCriadorId(Long criadorId) {
        this.criadorId = criadorId;
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
