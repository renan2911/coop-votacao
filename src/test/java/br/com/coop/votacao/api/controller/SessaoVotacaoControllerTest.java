package br.com.coop.votacao.api.controller;

import br.com.coop.votacao.api.controller.v1.SessaoVotacaoController;
import br.com.coop.votacao.entity.Pauta;
import br.com.coop.votacao.domain.SessaoStatus;
import br.com.coop.votacao.entity.SessaoVotacao;
import br.com.coop.votacao.service.SessaoVotacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SessaoVotacaoController.class)
@DisplayName("Testes de API - SessaoVotacaoController")
class SessaoVotacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SessaoVotacaoService sessaoVotacaoService;

    @Test
    @DisplayName("POST /sessoes - Deve abrir sessão com duração padrão")
    void deveAbrirSessaoComDuracaoPadrao() throws Exception {
        Pauta pauta = new Pauta();
        pauta.setId(1L);
        pauta.setTitulo("Pauta Teste");

        SessaoVotacao sessao = new SessaoVotacao();
        sessao.setId(10L);
        sessao.setPauta(pauta);
        sessao.setInicio(Instant.now());
        sessao.setFim(Instant.now().plusSeconds(60));
        sessao.setStatus(SessaoStatus.ABERTA);

        when(sessaoVotacaoService.abrirSessao(eq(1L), any())).thenReturn(sessao);

        String requestBody = "{}";

        mockMvc.perform(post("/api/v1/pautas/sessoes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("ABERTA"));
    }

    @Test
    @DisplayName("POST /sessoes - Deve abrir sessão com duração customizada")
    void deveAbrirSessaoComDuracaoCustomizada() throws Exception {
        Pauta pauta = new Pauta();
        pauta.setId(1L);

        SessaoVotacao sessao = new SessaoVotacao();
        sessao.setId(10L);
        sessao.setPauta(pauta);
        sessao.setInicio(Instant.now());
        sessao.setFim(Instant.now().plusSeconds(120));
        sessao.setStatus(SessaoStatus.ABERTA);

        when(sessaoVotacaoService.abrirSessao(1L, 120L)).thenReturn(sessao);

        String requestBody = """
                {
                    "duracaoSegundos": 120
                }
                """;

        mockMvc.perform(post("/api/v1/pautas/sessoes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("POST /sessoes - Deve retornar 400 quando pautaId é inválido")
    void deveRetornar400QuandoPautaIdInvalido() throws Exception {
        String requestBody = """
                {
                    "duracaoSegundos": 120
                }
                """;

        mockMvc.perform(post("/api/v1/pautas/sessoes/abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /sessoes/{id} - Deve buscar sessão por ID")
    void deveBuscarSessaoPorId() throws Exception {
        Pauta pauta = new Pauta();
        pauta.setId(1L);

        SessaoVotacao sessao = new SessaoVotacao();
        sessao.setId(10L);
        sessao.setPauta(pauta);
        sessao.setStatus(SessaoStatus.ABERTA);

        when(sessaoVotacaoService.buscarPorId(10L)).thenReturn(sessao);

        mockMvc.perform(get("/api/v1/pautas/sessoes/1/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("ABERTA"));
    }

    @Test
    @DisplayName("GET /resultado/{pautaId} - Deve retornar resultado APROVADA")
    void deveRetornarResultadoAprovada() throws Exception {
        Pauta pauta = new Pauta();
        pauta.setId(1L);

        SessaoVotacao sessao = new SessaoVotacao();
        sessao.setId(10L);
        sessao.setPauta(pauta);
        sessao.setStatus(SessaoStatus.ENCERRADA);
        sessao.setTotalSim(5);
        sessao.setTotalNao(3);

        when(sessaoVotacaoService.resultado(1L)).thenReturn(sessao);

        mockMvc.perform(get("/api/v1/pautas/resultado/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pautaId").value(1))
                .andExpect(jsonPath("$.sessaoId").value(10))
                .andExpect(jsonPath("$.totalSim").value(5))
                .andExpect(jsonPath("$.totalNao").value(3))
                .andExpect(jsonPath("$.resultado").value("APROVADA"));
    }

    @Test
    @DisplayName("GET /resultado/{pautaId} - Deve retornar resultado REPROVADA")
    void deveRetornarResultadoReprovada() throws Exception {
        Pauta pauta = new Pauta();
        pauta.setId(1L);

        SessaoVotacao sessao = new SessaoVotacao();
        sessao.setId(10L);
        sessao.setPauta(pauta);
        sessao.setStatus(SessaoStatus.ENCERRADA);
        sessao.setTotalSim(2);
        sessao.setTotalNao(7);

        when(sessaoVotacaoService.resultado(1L)).thenReturn(sessao);

        mockMvc.perform(get("/api/v1/pautas/resultado/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("REPROVADA"));
    }

    @Test
    @DisplayName("GET /resultado/{pautaId} - Deve retornar resultado EMPATE")
    void deveRetornarResultadoEmpate() throws Exception {
        Pauta pauta = new Pauta();
        pauta.setId(1L);

        SessaoVotacao sessao = new SessaoVotacao();
        sessao.setId(10L);
        sessao.setPauta(pauta);
        sessao.setStatus(SessaoStatus.ENCERRADA);
        sessao.setTotalSim(4);
        sessao.setTotalNao(4);

        when(sessaoVotacaoService.resultado(1L)).thenReturn(sessao);

        mockMvc.perform(get("/api/v1/pautas/resultado/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("EMPATE"));
    }
}
