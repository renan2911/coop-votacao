package br.com.coop.votacao.api.controller;

import br.com.coop.votacao.api.controller.v1.VotoController;
import br.com.coop.votacao.entity.Pauta;
import br.com.coop.votacao.entity.Voto;
import br.com.coop.votacao.domain.VotoValor;
import br.com.coop.votacao.service.VotoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VotoController.class)
@DisplayName("Testes de API - VotoController")
class VotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VotoService votoService;

    @Test
    @DisplayName("POST /votos - Deve registrar voto SIM com sucesso")
    void deveRegistrarVotoSimComSucesso() throws Exception {
        Pauta pauta = new Pauta();
        pauta.setId(1L);

        Voto voto = new Voto();
        voto.setId(1L);
        voto.setPauta(pauta);
        voto.setAssociadoId("12345678901");
        voto.setValor(VotoValor.SIM);

        when(votoService.registrarVoto(eq(1L), eq("12345678901"), eq(VotoValor.SIM)))
                .thenReturn(voto);

        String requestBody = """
                {
                    "cpf": "12345678901",
                    "voto": "SIM"
                }
                """;

        mockMvc.perform(post("/api/v1/pautas/votos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("POST /votos - Deve registrar voto NAO com sucesso")
    void deveRegistrarVotoNaoComSucesso() throws Exception {
        Pauta pauta = new Pauta();
        pauta.setId(1L);

        Voto voto = new Voto();
        voto.setId(2L);
        voto.setPauta(pauta);
        voto.setAssociadoId("98765432109");
        voto.setValor(VotoValor.NAO);

        when(votoService.registrarVoto(eq(1L), eq("98765432109"), eq(VotoValor.NAO)))
                .thenReturn(voto);

        String requestBody = """
                {
                    "cpf": "98765432109",
                    "voto": "NAO"
                }
                """;

        mockMvc.perform(post("/api/v1/pautas/votos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("POST /votos - Deve retornar 400 quando cpf é inválido")
    void deveRetornar400QuandoCpfInvalido() throws Exception {
        String requestBody = """
                {
                    "cpf": "123",
                    "voto": "SIM"
                }
                """;

        mockMvc.perform(post("/api/v1/pautas/votos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /votos - Deve retornar 400 quando cpf é nulo")
    void deveRetornar400QuandoCpfNulo() throws Exception {
        String requestBody = """
                {
                    "voto": "SIM"
                }
                """;

        mockMvc.perform(post("/api/v1/pautas/votos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /votos - Deve retornar 400 quando voto é nulo")
    void deveRetornar400QuandoVotoNulo() throws Exception {
        String requestBody = """
                {
                    "cpf": "12345678901"
                }
                """;

        mockMvc.perform(post("/api/v1/pautas/votos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
