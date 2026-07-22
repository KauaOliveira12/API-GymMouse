package com.streaks.fit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaks.fit.repository.DispositivoPushRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExpoPushService {

    private static final Logger log = LoggerFactory.getLogger(ExpoPushService.class);
    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final DispositivoPushRepository dispositivoPushRepository;

    public ExpoPushService(DispositivoPushRepository dispositivoPushRepository, ObjectMapper objectMapper) {
        this.dispositivoPushRepository = dispositivoPushRepository;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().baseUrl(EXPO_PUSH_URL).build();
    }

    public void enviar(List<String> tokens, String titulo, String corpo, Map<String, Object> data) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        List<Map<String, Object>> mensagens = new ArrayList<>();
        for (String token : tokens) {
            if (token == null || token.isBlank()) {
                continue;
            }
            Map<String, Object> msg = new HashMap<>();
            msg.put("to", token.trim());
            msg.put("sound", "default");
            msg.put("title", titulo);
            msg.put("body", corpo);
            msg.put("priority", "high");
            if (data != null && !data.isEmpty()) {
                msg.put("data", data);
            }
            mensagens.add(msg);
        }
        if (mensagens.isEmpty()) {
            return;
        }

        try {
            String resposta = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(mensagens)
                    .retrieve()
                    .body(String.class);
            limparTokensInvalidos(resposta, mensagens);
        } catch (Exception e) {
            log.warn("Falha ao enviar push Expo: {}", e.getMessage());
        }
    }

    private void limparTokensInvalidos(String respostaJson, List<Map<String, Object>> mensagens) {
        if (respostaJson == null || respostaJson.isBlank()) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(respostaJson);
            JsonNode data = root.path("data");
            if (!data.isArray()) {
                return;
            }
            for (int i = 0; i < data.size() && i < mensagens.size(); i++) {
                JsonNode item = data.get(i);
                String status = item.path("status").asText("");
                String error = item.path("details").path("error").asText(item.path("message").asText(""));
                if ("error".equalsIgnoreCase(status)
                        && (error.contains("DeviceNotRegistered") || error.contains("InvalidCredentials"))) {
                    Object to = mensagens.get(i).get("to");
                    if (to != null) {
                        dispositivoPushRepository.deleteByToken(String.valueOf(to));
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Nao foi possivel interpretar resposta Expo: {}", e.getMessage());
        }
    }
}
