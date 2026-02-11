package br.com.coop.votacao.api.controller;

import br.com.coop.votacao.api.controller.v1.PautaController;
import br.com.coop.votacao.entity.Pauta;
import br.com.coop.votacao.service.PautaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PautaController.class)
@DisplayName("Testes de API - PautaController")
class PautaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PautaService pautaService;

    @Test
    @DisplayName("POST /pautas - Deve criar pauta com sucesso")
    void deveCriarPautaComSucesso() throws Exception {
        Pauta pauta = new Pauta();
        pauta.setId(1L);
        pauta.setTitulo("Construção de Nova Sede");
        pauta.setDescricao("Aprovação para construção");
        pauta.setDataCriacao(Instant.now());

        when(pautaService.criar(any(Pauta.class))).thenReturn(pauta);

        String requestBody = """
                {
                    "titulo": "Construção de Nova Sede",
                    "descricao": "Aprovação para construção"
                }
                """;

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Construção de Nova Sede"))
                .andExpect(jsonPath("$.descricao").value("Aprovação para construção"));
    }

    @Test
    @DisplayName("POST /pautas - Deve retornar 400 quando título está vazio")
    void deveRetornar400QuandoTituloVazio() throws Exception {
        String requestBody = """
                {
                    "titulo": "",
                    "descricao": "Descrição válida"
                }
                """;

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /pautas/{id} - Deve buscar pauta por ID")
    void deveBuscarPautaPorId() throws Exception {
        Pauta pauta = new Pauta();
        pauta.setId(1L);
        pauta.setTitulo("Pauta Teste");
        pauta.setDataCriacao(Instant.now());

        when(pautaService.buscarPorId(1L)).thenReturn(pauta);


        mockMvc.perform(get("/api/v1/pautas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Pauta Teste"));
    }

    @Test
    @DisplayName("GET /pautas - Deve listar todas as pautas")
    void deveListarTodasAsPautas() throws Exception {
        Pauta pauta1 = new Pauta();
        pauta1.setId(1L);
        pauta1.setTitulo("Pauta 1");

        Pauta pauta2 = new Pauta();
        pauta2.setId(2L);
        pauta2.setTitulo("Pauta 2");

        when(pautaService.listar()).thenReturn(Arrays.asList(pauta1, pauta2));

        mockMvc.perform(get("/api/v1/pautas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].titulo").value("Pauta 1"))
                .andExpect(jsonPath("$[1].titulo").value("Pauta 2"));
    }
}
