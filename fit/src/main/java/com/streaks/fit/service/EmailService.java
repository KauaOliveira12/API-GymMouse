package com.streaks.fit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.from:}")
    private String from;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    public boolean enviarCodigoRecuperacao(String destino, String codigo) {
        String assunto = "Streaks - Código para redefinir senha";
        String corpo = """
                Olá!

                Você pediu para redefinir a senha da sua conta no Streaks.
                Use o código abaixo no aplicativo:

                %s

                Este código expira em 15 minutos.
                Se você não solicitou isso, ignore este e-mail.
                """.formatted(codigo);

        if (!mailEnabled || mailSender == null || from == null || from.isBlank()) {
            log.warn(
                    "E-mail de recuperação NÃO enviado (mail desabilitado ou sem SMTP). Destino={}, codigo={}",
                    destino,
                    codigo
            );
            return false;
        }

        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setFrom(from);
            mensagem.setTo(destino);
            mensagem.setSubject(assunto);
            mensagem.setText(corpo);
            mailSender.send(mensagem);
            log.info("E-mail de recuperação enviado para {}", destino);
            return true;
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de recuperação para {}: {}", destino, e.getMessage());
            return false;
        }
    }
}
