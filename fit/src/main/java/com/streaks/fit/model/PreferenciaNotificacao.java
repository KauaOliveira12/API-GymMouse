package com.streaks.fit.model;

import jakarta.persistence.*;

@Entity
@Table(name = "preferencias_notificacao")
public class PreferenciaNotificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false)
    private boolean checkins = true;

    @Column(nullable = false)
    private boolean grupos = true;

    @Column(nullable = false)
    private boolean ranking = true;

    @Column(nullable = false)
    private boolean lembretes = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public boolean isCheckins() {
        return checkins;
    }

    public void setCheckins(boolean checkins) {
        this.checkins = checkins;
    }

    public boolean isGrupos() {
        return grupos;
    }

    public void setGrupos(boolean grupos) {
        this.grupos = grupos;
    }

    public boolean isRanking() {
        return ranking;
    }

    public void setRanking(boolean ranking) {
        this.ranking = ranking;
    }

    public boolean isLembretes() {
        return lembretes;
    }

    public void setLembretes(boolean lembretes) {
        this.lembretes = lembretes;
    }
}
