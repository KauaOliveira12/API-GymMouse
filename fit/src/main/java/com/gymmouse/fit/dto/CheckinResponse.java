package com.gymmouse.fit.dto;

import com.gymmouse.fit.model.Checkin;

import java.time.LocalDateTime;

public class CheckinResponse {

    private Long id;
    private Long usuarioId;
    private Long grupoId;
    private String nome;
    private String tempo;
    private String titulo;
    private String desc;
    private String imagem;
    private int likes;
    private int comments;
    private boolean curtido;
    private int pontos;
    private LocalDateTime dataCriacao;

    public CheckinResponse() {
    }

    public CheckinResponse(Checkin checkin) {
        this(checkin, 0, 0, false);
    }

    public CheckinResponse(Checkin checkin, long likes, long comments, boolean curtido) {
        this.id = checkin.getId();
        this.usuarioId = checkin.getUsuario().getId();
        this.grupoId = checkin.getGrupo().getId();
        this.nome = checkin.getUsuario().getNome();
        this.tempo = "Agora mesmo";
        this.titulo = checkin.getTitulo();
        this.desc = checkin.getDescricao();
        this.imagem = checkin.getImagem();
        this.likes = (int) likes;
        this.comments = (int) comments;
        this.curtido = curtido;
        this.pontos = checkin.getPontos();
        this.dataCriacao = checkin.getDataCriacao();
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getGrupoId() {
        return grupoId;
    }

    public String getNome() {
        return nome;
    }

    public String getTempo() {
        return tempo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDesc() {
        return desc;
    }

    public String getImagem() {
        return imagem;
    }

    public int getLikes() {
        return likes;
    }

    public int getComments() {
        return comments;
    }

    public boolean isCurtido() {
        return curtido;
    }

    public int getPontos() {
        return pontos;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
}
