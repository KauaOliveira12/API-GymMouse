package com.gymmouse.fit.dto;

public record CriarMensagemRequest(
        String conteudo,
        Long usuarioId,
        Long grupoId
) {}