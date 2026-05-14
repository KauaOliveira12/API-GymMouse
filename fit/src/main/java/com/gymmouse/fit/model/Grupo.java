package com.gymmouse.fit.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "grupos")
public class Grupo {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String descricao;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(unique = true)
    private String codigoAcesso;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "criador_id", nullable = false)
    private Usuario criador;

    @Transient
    private Long totalMembros;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getCodigoAcesso() { return codigoAcesso; }
    public void setCodigoAcesso(String codigoAcesso) { this.codigoAcesso = codigoAcesso; }

    public Usuario getCriador() { return criador; }
    public void setCriador(Usuario criador) { this.criador = criador; }

    public Long getTotalMembros() { return totalMembros; }
    public void setTotalMembros(Long totalMembros) { this.totalMembros = totalMembros; }
}