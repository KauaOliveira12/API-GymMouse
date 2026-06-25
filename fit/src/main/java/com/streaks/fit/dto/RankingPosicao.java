package com.streaks.fit.dto;

public class RankingPosicao {

    private int posicao;
    private Long usuarioId;
    private String nomeUsuario;
    private long pontosDesdeEntrada;

    public RankingPosicao() {
    }

    public RankingPosicao(int posicao, Long usuarioId, String nomeUsuario, long pontosDesdeEntrada) {
        this.posicao = posicao;
        this.usuarioId = usuarioId;
        this.nomeUsuario = nomeUsuario;
        this.pontosDesdeEntrada = pontosDesdeEntrada;
    }

    public int getPosicao() {
        return posicao;
    }

    public void setPosicao(int posicao) {
        this.posicao = posicao;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public long getPontosDesdeEntrada() {
        return pontosDesdeEntrada;
    }

    public void setPontosDesdeEntrada(long pontosDesdeEntrada) {
        this.pontosDesdeEntrada = pontosDesdeEntrada;
    }
}
