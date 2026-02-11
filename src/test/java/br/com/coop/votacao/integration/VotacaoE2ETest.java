package br.com.coop.votacao.integration;

import br.com.coop.votacao.entity.Pauta;
import br.com.coop.votacao.domain.SessaoStatus;
import br.com.coop.votacao.domain.VotoValor;
import br.com.coop.votacao.messaging.ResultadoPautaProducer;
import br.com.coop.votacao.repository.PautaRepository;
import br.com.coop.votacao.repository.SessaoVotacaoRepository;
import br.com.coop.votacao.repository.VotoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("Testes de Integração E2E - Fluxo Completo de Votação")
class VotacaoE2ETest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PautaRepository pautaRepository;

    @Autowired
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Autowired
    private VotoRepository votoRepository;

    @MockBean
    private ResultadoPautaProducer resultadoPautaProducer;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        votoRepository.deleteAll();
        sessaoVotacaoRepository.deleteAll();
        pautaRepository.deleteAll();

        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();

        doNothing().when(resultadoPautaProducer).publicar(any());
    }

    @AfterEach
    void tearDown() {
        votoRepository.deleteAll();
        sessaoVotacaoRepository.deleteAll();
        pautaRepository.deleteAll();
    }

    @Test
    @DisplayName("Fluxo completo: Criar pauta → Abrir sessão → Votar → Verificar resultado")
    void fluxoCompletoDeVotacao() throws Exception {
        String criarPautaRequest = """
                {
                    "titulo": "Construção de Nova Sede",
                    "descricao": "Aprovar investimento para construção"
                }
                """;

        MvcResult pautaResult = mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarPautaRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.titulo").value("Construção de Nova Sede"))
                .andReturn();

        String pautaResponse = pautaResult.getResponse().getContentAsString();
        Long pautaId = objectMapper.readTree(pautaResponse).get("id").asLong();

        assertThat(pautaRepository.findById(pautaId)).isPresent();

        String abrirSessaoRequest = """
                {
                    "duracaoSegundos": 300
                }
                """;

        MvcResult sessaoResult = mockMvc.perform(post("/api/v1/pautas/sessoes/" + pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(abrirSessaoRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andReturn();

        String sessaoResponse = sessaoResult.getResponse().getContentAsString();
        Long sessaoId = objectMapper.readTree(sessaoResponse).get("id").asLong();

        assertThat(sessaoVotacaoRepository.findById(sessaoId))
                .isPresent()
                .get()
                .satisfies(sessao -> {
                    assertThat(sessao.getStatus()).isEqualTo(SessaoStatus.ABERTA);
                    assertThat(sessao.getPauta().getId()).isEqualTo(pautaId);
                });

        String[] associados = {"12345678901", "12345678902", "12345678903", "12345678904", "12345678905"};
        VotoValor[] votos = {VotoValor.SIM, VotoValor.SIM, VotoValor.SIM, VotoValor.NAO, VotoValor.NAO};

        for (int i = 0; i < associados.length; i++) {
            String votoRequest = String.format("""
                    {
                        "cpf": "%s",
                        "voto": "%s"
                    }
                    """, associados[i], votos[i]);

            mockMvc.perform(post("/api/v1/pautas/votos/" + pautaId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(votoRequest))
                    .andExpect(status().isCreated());
        }


        assertThat(votoRepository.count()).isEqualTo(5);
        assertThat(votoRepository.countByPautaIdAndValor(pautaId, VotoValor.SIM)).isEqualTo(3);
        assertThat(votoRepository.countByPautaIdAndValor(pautaId, VotoValor.NAO)).isEqualTo(2);

        mockMvc.perform(get("/api/v1/pautas/" + pautaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pautaId))
                .andExpect(jsonPath("$.titulo").value("Construção de Nova Sede"));

        mockMvc.perform(get("/api/v1/pautas/sessoes/" + pautaId + "/" + sessaoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessaoId))
                .andExpect(jsonPath("$.status").value("ABERTA"));
    }

    @Test
    @DisplayName("Deve impedir voto duplicado do mesmo associado")
    void deveImpedirVotoDuplicado() throws Exception {
        Pauta pauta = new Pauta();
        pauta.setTitulo("Pauta Teste");
        pauta = pautaRepository.save(pauta);

        String abrirSessaoRequest = """
                {
                    "duracaoSegundos": 300
                }
                """;

        mockMvc.perform(post("/api/v1/pautas/sessoes/" + pauta.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(abrirSessaoRequest))
                .andExpect(status().isCreated());

        String votoRequest = """
                {
                    "cpf": "12345678901",
                    "voto": "SIM"
                }
                """;

        mockMvc.perform(post("/api/v1/pautas/votos/" + pauta.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(votoRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/pautas/votos/" + pauta.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(votoRequest))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(containsString("já votou")));

        assertThat(votoRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve listar múltiplas pautas")
    void deveListarMultiplasPautas() throws Exception {
        for (int i = 1; i <= 3; i++) {
            Pauta pauta = new Pauta();
            pauta.setTitulo("Pauta " + i);
            pauta.setDescricao("Descrição " + i);
            pautaRepository.save(pauta);
        }

        mockMvc.perform(get("/api/v1/pautas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].titulo").exists())
                .andExpect(jsonPath("$[1].titulo").exists())
                .andExpect(jsonPath("$[2].titulo").exists());
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar pauta inexistente")
    void deveRetornar404AoBuscarPautaInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/pautas/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("não encontrada")));
    }
}
