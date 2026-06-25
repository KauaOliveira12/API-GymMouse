package com.streaks.fit.dto;

import java.time.LocalDateTime;

public class EntradaGrupoResponse {

    private Long id;
    private Long grupoId;
    private String nomeGrupo;
    private Long usuarioId;
    private LocalDateTime dataEntrada;

    public EntradaGrupoResponse() {
    }

    public EntradaGrupoResponse(Long id, Long grupoId, String nomeGrupo, Long usuarioId, LocalDateTime dataEntrada) {
        this.id = id;
        this.grupoId = grupoId;
        this.nomeGrupo = nomeGrupo;
        this.usuarioId = usuarioId;
        this.dataEntrada = dataEntrada;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(Long grupoId) {
        this.grupoId = grupoId;
    }

    public String getNomeGrupo() {
        return nomeGrupo;
    }

    public void setNomeGrupo(String nomeGrupo) {
        this.nomeGrupo = nomeGrupo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDateTime dataEntrada) {
        this.dataEntrada = dataEntrada;
    }
}
