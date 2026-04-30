package com.gymmouse.fit.model; // Atenção: mude 'com.seuprojeto.gymmouse' para o nome da sua pasta base!

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true)
    private String email;

    private String senha;

    private int pontosTotais = 0;

    // Métodos para o Java conseguir ler e alterar as variáveis (Getters e Setters)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public int getPontosTotais() { return pontosTotais; }
    public void setPontosTotais(int pontosTotais) { this.pontosTotais = pontosTotais; }
}
