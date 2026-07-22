package com.streaks.fit.dto;

import com.streaks.fit.model.PreferenciaNotificacao;

public class PreferenciasNotificacaoResponse {

    private boolean checkins;
    private boolean grupos;
    private boolean ranking;
    private boolean lembretes;

    public PreferenciasNotificacaoResponse() {
    }

    public PreferenciasNotificacaoResponse(PreferenciaNotificacao pref) {
        this.checkins = pref.isCheckins();
        this.grupos = pref.isGrupos();
        this.ranking = pref.isRanking();
        this.lembretes = pref.isLembretes();
    }

    public static PreferenciasNotificacaoResponse padrao() {
        PreferenciasNotificacaoResponse r = new PreferenciasNotificacaoResponse();
        r.checkins = true;
        r.grupos = true;
        r.ranking = true;
        r.lembretes = true;
        return r;
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
