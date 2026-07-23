package com.streaks.fit.dto;

import com.streaks.fit.model.CheckinComentario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ComentarioResponse {

    private Long id;
    private Long usuarioId;
    private String nome;
    private String fotoPerfil;
    private String texto;
    private LocalDateTime dataCriacao;
    private Long comentarioPaiId;
    private List<ComentarioResponse> respostas = new ArrayList<>();

    public ComentarioResponse(CheckinComentario comentario) {
        this.id = comentario.getId();
        this.usuarioId = comentario.getUsuario().getId();
        this.nome = comentario.getUsuario().getNome();
        this.fotoPerfil = comentario.getUsuario().getFotoPerfil();
        this.texto = comentario.getTexto();
        this.dataCriacao = comentario.getDataCriacao();
        this.comentarioPaiId = comentario.getComentarioPai() == null ? null : comentario.getComentarioPai().getId();
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getNome() {
        return nome;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public String getTexto() {
        return texto;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public Long getComentarioPaiId() {
        return comentarioPaiId;
    }

    public List<ComentarioResponse> getRespostas() {
        return respostas;
    }

    public void setRespostas(List<ComentarioResponse> respostas) {
        this.respostas = respostas;
    }
}
