package com.streaks.fit.dto;

public class PreferenciasNotificacaoRequest {

    private Boolean checkins;
    private Boolean grupos;
    private Boolean ranking;
    private Boolean lembretes;

    public Boolean getCheckins() {
        return checkins;
    }

    public void setCheckins(Boolean checkins) {
        this.checkins = checkins;
    }

    public Boolean getGrupos() {
        return grupos;
    }

    public void setGrupos(Boolean grupos) {
        this.grupos = grupos;
    }

    public Boolean getRanking() {
        return ranking;
    }

    public void setRanking(Boolean ranking) {
        this.ranking = ranking;
    }

    public Boolean getLembretes() {
        return lembretes;
    }

    public void setLembretes(Boolean lembretes) {
        this.lembretes = lembretes;
    }
}
