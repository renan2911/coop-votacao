package br.com.coop.votacao.service;

import br.com.coop.votacao.entity.Pauta;
import br.com.coop.votacao.domain.SessaoStatus;
import br.com.coop.votacao.entity.SessaoVotacao;
import br.com.coop.votacao.entity.Voto;
import br.com.coop.votacao.domain.VotoValor;
import br.com.coop.votacao.exception.BusinessException;
import br.com.coop.votacao.repository.VotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - VotoService")
class VotoServiceTest {

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private PautaService pautaService;

    @Mock
    private SessaoVotacaoService sessaoVotacaoService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private DefaultRedisScript<Long> incrementWithTtlScript;

    @InjectMocks
    private VotoService votoService;

    private Pauta pauta;
    private SessaoVotacao sessao;
    private Voto voto;

    @BeforeEach
    void setUp() {
        pauta = new Pauta();
        pauta.setId(1L);
        pauta.setTitulo("Pauta Teste");

        sessao = new SessaoVotacao();
        sessao.setId(10L);
        sessao.setPauta(pauta);
        sessao.setInicio(Instant.now().minusSeconds(300));
        sessao.setFim(Instant.now().plusSeconds(300));
        sessao.setStatus(SessaoStatus.ABERTA);

        voto = new Voto();
        voto.setId(1L);
        voto.setPauta(pauta);
        voto.setAssociadoId("12345678901");
        voto.setValor(VotoValor.SIM);
    }

    @Test
    @DisplayName("Deve registrar voto SIM com sucesso")
    void deveRegistrarVotoSimComSucesso() {
        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(sessaoVotacaoService.buscarSessaoAbertaPorPauta(1L)).thenReturn(sessao);
        when(votoRepository.saveAndFlush(any(Voto.class))).thenReturn(voto);
        when(redisTemplate.execute(any(), anyList(), anyString())).thenReturn(1L);

        Voto resultado = votoService.registrarVoto(1L, "12345678901", VotoValor.SIM);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getValor()).isEqualTo(VotoValor.SIM);
        verify(votoRepository, times(1)).saveAndFlush(any(Voto.class));
    }

    @Test
    @DisplayName("Deve registrar voto NAO com sucesso")
    void deveRegistrarVotoNaoComSucesso() {
        voto.setValor(VotoValor.NAO);
        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(sessaoVotacaoService.buscarSessaoAbertaPorPauta(1L)).thenReturn(sessao);
        when(votoRepository.saveAndFlush(any(Voto.class))).thenReturn(voto);
        when(redisTemplate.execute(any(), anyList(), anyString())).thenReturn(1L);

        Voto resultado = votoService.registrarVoto(1L, "12345678901", VotoValor.NAO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getValor()).isEqualTo(VotoValor.NAO);
    }

    @Test
    @DisplayName("Deve lançar exceção quando sessão está encerrada")
    void deveLancarExcecaoQuandoSessaoEstEncerrada() {
        sessao.setFim(Instant.now().minusSeconds(60));
        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(sessaoVotacaoService.buscarSessaoAbertaPorPauta(1L)).thenReturn(sessao);

        assertThatThrownBy(() -> votoService.registrarVoto(1L, "12345678901", VotoValor.SIM))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("está encerrada");

        verify(votoRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando associado já votou")
    void deveLancarExcecaoQuandoAssociadoJaVotou() {
        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(sessaoVotacaoService.buscarSessaoAbertaPorPauta(1L)).thenReturn(sessao);
        when(votoRepository.saveAndFlush(any(Voto.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        assertThatThrownBy(() -> votoService.registrarVoto(1L, "12345678901", VotoValor.SIM))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já votou");
    }
}
