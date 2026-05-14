package com.gymmouse.fit.dto;

/**
 * Corpo mínimo para criar grupo (ex.: app mobile): use
 * {@code POST /api/usuarios/{id}/grupos} ou {@code POST /api/grupos} com header {@code X-Usuario-Id}
 * e corpo só com nome e descrição.
 */
public class CriarGrupoCorpoLeve {

    private String nome;
    private String descricao;

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
}
