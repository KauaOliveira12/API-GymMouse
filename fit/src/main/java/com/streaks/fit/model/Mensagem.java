package com.streaks.fit.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter; // Certifique-se de ter o Lombok no pom.xml
import lombok.Setter;

@Entity
@Getter // Cria os métodos GET automaticamente
@Setter // Cria os métodos SET automaticamente
public class Mensagem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String conteudo;
    private LocalDateTime dataEnvio;

    @ManyToOne
    @JoinColumn(name = "usuario_id") // Nome da coluna no banco
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;
}