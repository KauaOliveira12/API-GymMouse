package com.gymmouse.fit.dto;

import com.gymmouse.fit.model.CheckinComentario;

import java.time.LocalDateTime;

public class ComentarioResponse {

    private Long id;
    private Long usuarioId;
    private String nome;
    private String texto;
    private LocalDateTime dataCriacao;

    public ComentarioResponse(CheckinComentario comentario) {
        this.id = comentario.getId();
        this.usuarioId = comentario.getUsuario().getId();
        this.nome = comentario.getUsuario().getNome();
        this.texto = comentario.getTexto();
        this.dataCriacao = comentario.getDataCriacao();
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

    public String getTexto() {
        return texto;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
}
