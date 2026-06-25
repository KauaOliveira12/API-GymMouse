package com.streaks.fit.model;

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

    @Lob
    @Column(name = "imagem_capa", columnDefinition = "LONGTEXT")
    private String imagemCapa;

    @Column(name = "pontos_por_checkin", nullable = false)
    private Integer pontosPorCheckin = 1;

    /**
     * Dias consecutivos de check-in necessários para aplicar o multiplicador (ex.: 3).
     * {@code null} desativa o bônus de sequência.
     */
    @Column(name = "dias_sequencia_bonus")
    private Integer diasSequenciaParaBonus;

    /**
     * Multiplicador aplicado quando a sequência atinge {@link #diasSequenciaParaBonus} (ex.: 2.0 = dobrar).
     */
    @Column(name = "multiplicador_sequencia")
    private Double multiplicadorSequencia;

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

    public String getImagemCapa() { return imagemCapa; }
    public void setImagemCapa(String imagemCapa) { this.imagemCapa = imagemCapa; }

    public Integer getPontosPorCheckin() { return pontosPorCheckin; }
    public void setPontosPorCheckin(Integer pontosPorCheckin) { this.pontosPorCheckin = pontosPorCheckin; }

    public Integer getDiasSequenciaParaBonus() { return diasSequenciaParaBonus; }
    public void setDiasSequenciaParaBonus(Integer diasSequenciaParaBonus) { this.diasSequenciaParaBonus = diasSequenciaParaBonus; }

    public Double getMultiplicadorSequencia() { return multiplicadorSequencia; }
    public void setMultiplicadorSequencia(Double multiplicadorSequencia) { this.multiplicadorSequencia = multiplicadorSequencia; }

    public String getCodigoAcesso() { return codigoAcesso; }
    public void setCodigoAcesso(String codigoAcesso) { this.codigoAcesso = codigoAcesso; }

    public Usuario getCriador() { return criador; }
    public void setCriador(Usuario criador) { this.criador = criador; }

    public Long getTotalMembros() { return totalMembros; }
    public void setTotalMembros(Long totalMembros) { this.totalMembros = totalMembros; }
}