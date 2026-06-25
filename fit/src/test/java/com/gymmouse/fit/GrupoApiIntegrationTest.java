package com.gymmouse.fit;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GrupoApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static long readLong(String json, String path) {
        Object v = JsonPath.read(json, path);
        if (v instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(v.toString());
    }

    private static String readString(String json, String path) {
        return JsonPath.read(json, path);
    }

    private long criarUsuario(String sufixoEmail) throws Exception {
        String email = "u_" + sufixoEmail + "_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        String body = """
                {"nome":"Usuario","email":"%s","senha":"123"}
                """.formatted(email);
        MvcResult r = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return readLong(r.getResponse().getContentAsString(), "$.id");
    }

    private String criarGrupoJson(long criadorId, String nome) throws Exception {
        String body = """
                {"nome":"%s","descricao":"d","criadorId":%d}
                """.formatted(nome, criadorId);
        MvcResult r = mockMvc.perform(post("/api/grupos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return r.getResponse().getContentAsString();
    }

    @Test
    void criarGrupo_retornaCodigoECriador() throws Exception {
        long criador = criarUsuario("c1");
        String g = criarGrupoJson(criador, "Grupo Alpha");
        assertThat(readLong(g, "$.id")).isPositive();
        assertThat(readString(g, "$.codigoAcesso")).isNotBlank();
        assertThat(readLong(g, "$.criador.id")).isEqualTo(criador);
    }

    @Test
    void criarCheckin_comLocalizacao_retornaCoordenadas() throws Exception {
        long usuario = criarUsuario("checkinloc");
        String g = criarGrupoJson(usuario, "Grupo Checkin Local");
        long grupoId = readLong(g, "$.id");

        String body = """
                {
                  "usuarioId":%d,
                  "grupoId":%d,
                  "titulo":"Treino",
                  "descricao":"Peito e triceps",
                  "imagem":"base64fake",
                  "latitude":-23.55052,
                  "longitude":-46.633308,
                  "localizacao":"Sao Paulo"
                }
                """.formatted(usuario, grupoId);

        mockMvc.perform(post("/api/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(-23.55052))
                .andExpect(jsonPath("$.longitude").value(-46.633308))
                .andExpect(jsonPath("$.localizacao").value("Sao Paulo"));
    }

    @Test
    void criarGrupo_criadorInexistente_retorna404() throws Exception {
        String body = """
                {"nome":"X","descricao":"","criadorId":999999}
                """;
        mockMvc.perform(post("/api/grupos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void criarGrupo_soNomeEDescricao_viaUsuario_retorna200() throws Exception {
        long criador = criarUsuario("c6");
        mockMvc.perform(post("/api/usuarios/" + criador + "/grupos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Treino RN\",\"descricao\":\"Só malhar\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoAcesso").exists())
                .andExpect(jsonPath("$.criador.id").value(criador));
    }

    @Test
    void criarGrupo_soNomeEDescricao_comQueryCriadorId_retorna200() throws Exception {
        long criador = criarUsuario("c7");
        mockMvc.perform(post("/api/grupos").param("criadorId", String.valueOf(criador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Via Query\",\"descricao\":\"ok\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criador.id").value(criador));
    }

    @Test
    void criarGrupo_soNomeEDescricao_comHeaderXUsuarioId_retorna200() throws Exception {
        long criador = criarUsuario("c8");
        mockMvc.perform(post("/api/grupos")
                        .header("X-Usuario-Id", String.valueOf(criador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Só corpo\",\"descricao\":\"header\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.codigoAcesso").exists())
                .andExpect(jsonPath("$.criador.id").value(criador));
    }

    @Test
    void entrarESairDoGrupo() throws Exception {
        long criador = criarUsuario("c2");
        long outro = criarUsuario("o2");
        String gJson = criarGrupoJson(criador, "Grupo Beta");
        long grupoId = readLong(gJson, "$.id");
        String codigo = readString(gJson, "$.codigoAcesso");

        mockMvc.perform(post("/api/grupos/entrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":" + outro + ",\"codigoAcesso\":\"" + codigo + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(outro))
                .andExpect(jsonPath("$.grupoId").value(grupoId));

        mockMvc.perform(post("/api/grupos/entrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":" + outro + ",\"codigoAcesso\":\"" + codigo + "\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/grupos/sair")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":" + outro + ",\"grupoId\":" + grupoId + "}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/grupos/sair")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":" + outro + ",\"grupoId\":" + grupoId + "}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizarGrupo_apenasCriadorPodeEditar() throws Exception {
        long criador = criarUsuario("c3");
        long estranho = criarUsuario("e3");
        String gJson = criarGrupoJson(criador, "Grupo Gamma");
        long grupoId = readLong(gJson, "$.id");

        mockMvc.perform(put("/api/grupos/" + grupoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":" + estranho + ",\"nome\":\"Hack\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/grupos/" + grupoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":" + criador + ",\"nome\":\"Gamma Novo\",\"descricao\":\"ok\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Gamma Novo"))
                .andExpect(jsonPath("$.descricao").value("ok"));
    }

    @Test
    void deletarGrupo_apenasCriadorPodeRemover() throws Exception {
        long criador = criarUsuario("c4");
        long estranho = criarUsuario("e4");
        String gJson = criarGrupoJson(criador, "Grupo Delta");
        long grupoId = readLong(gJson, "$.id");

        mockMvc.perform(delete("/api/grupos/" + grupoId).param("usuarioId", String.valueOf(estranho)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/grupos/" + grupoId).param("usuarioId", String.valueOf(criador)))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/grupos/" + grupoId).param("usuarioId", String.valueOf(criador)))
                .andExpect(status().isNotFound());
    }

    @Test
    void criarDoisGrupos_geraCodigosDistintos() throws Exception {
        long criador = criarUsuario("c5");
        String g1 = criarGrupoJson(criador, "Grupo Um");
        String g2 = criarGrupoJson(criador, "Grupo Dois");
        String c1 = readString(g1, "$.codigoAcesso");
        String c2 = readString(g2, "$.codigoAcesso");
        assertThat(c1).isNotBlank();
        assertThat(c2).isNotBlank();
        assertThat(c1).isNotEqualTo(c2);
    }

    @Test
    void listarGrupos_doUsuarioSemHeader_retorna400() throws Exception {
        mockMvc.perform(get("/api/grupos").param("doUsuario", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarGrupos_doUsuario_retornaSomenteMembros() throws Exception {
        long a = criarUsuario("listA");
        long b = criarUsuario("listB");
        String gA = criarGrupoJson(a, "AAA Meu Grupo");
        String gB = criarGrupoJson(b, "BBB Outro");
        String codB = readString(gB, "$.codigoAcesso");
        long idA = readLong(gA, "$.id");
        long idB = readLong(gB, "$.id");

        MvcResult r1 = mockMvc.perform(get("/api/grupos").param("doUsuario", "true")
                        .header("X-Usuario-Id", String.valueOf(a)))
                .andExpect(status().isOk())
                .andReturn();
        List<?> ids1 = JsonPath.read(r1.getResponse().getContentAsString(), "$.[*].id");
        assertThat(ids1.stream().map(o -> ((Number) o).longValue())).contains(idA);
        assertThat(ids1.stream().map(o -> ((Number) o).longValue())).doesNotContain(idB);

        mockMvc.perform(post("/api/grupos/entrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":" + a + ",\"codigoAcesso\":\"" + codB + "\"}"))
                .andExpect(status().isOk());

        MvcResult r2 = mockMvc.perform(get("/api/grupos").param("doUsuario", "true")
                        .header("X-Usuario-Id", String.valueOf(a)))
                .andExpect(status().isOk())
                .andReturn();
        List<?> ids2 = JsonPath.read(r2.getResponse().getContentAsString(), "$.[*].id");
        assertThat(ids2.stream().map(o -> ((Number) o).longValue())).contains(idA, idB);
    }
}
