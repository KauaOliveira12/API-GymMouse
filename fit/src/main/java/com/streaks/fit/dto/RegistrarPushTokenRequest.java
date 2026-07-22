package com.streaks.fit.dto;

public class RegistrarPushTokenRequest {

    private String token;
    private String plataforma;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }
}
