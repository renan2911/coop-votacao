package br.com.coop.votacao.service;

import br.com.coop.votacao.domain.SessaoStatus;
import br.com.coop.votacao.domain.VotoValor;
import br.com.coop.votacao.entity.Pauta;
import br.com.coop.votacao.entity.SessaoVotacao;
import br.com.coop.votacao.messaging.ResultadoPautaEvent;
import br.com.coop.votacao.messaging.ResultadoPautaProducer;
import br.com.coop.votacao.repository.SessaoVotacaoRepository;
import br.com.coop.votacao.repository.VotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - SessaoEncerramentoService")
class SessaoEncerramentoServiceTest {

    @Mock
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ResultadoPautaProducer resultadoPautaProducer;

    @Captor
    private ArgumentCaptor<ResultadoPautaEvent> eventCaptor;

    @InjectMocks
    private SessaoEncerramentoService sessaoEncerramentoService;

    private SessaoVotacao sessao;
    private Pauta pauta;

    @BeforeEach
    void setUp() {
        pauta = new Pauta();
        pauta.setId(1L);
        pauta.setTitulo("Pauta Teste");

        sessao = new SessaoVotacao();
        sessao.setId(10L);
        sessao.setPauta(pauta);
        sessao.setInicio(Instant.now().minusSeconds(600));
        sessao.setFim(Instant.now().minusSeconds(1));
        sessao.setStatus(SessaoStatus.ABERTA);
        sessao.setTotalSim(0);
        sessao.setTotalNao(0);
    }

    @Test
    @DisplayName("Deve encerrar sessão com contadores do Redis")
    void deveEncerrarSessaoComContadoresDoRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.multiGet(anyList())).thenReturn(List.of("7", "3"));
        when(sessaoVotacaoRepository.save(any(SessaoVotacao.class))).thenReturn(sessao);
        doNothing().when(resultadoPautaProducer).publicar(any());

        sessaoEncerramentoService.encerrar(sessao);

        assertThat(sessao.getStatus()).isEqualTo(SessaoStatus.ENCERRADA);
        assertThat(sessao.getTotalSim()).isEqualTo(7);
        assertThat(sessao.getTotalNao()).isEqualTo(3);

        verify(sessaoVotacaoRepository).save(sessao);
        verify(resultadoPautaProducer).publicar(eventCaptor.capture());

        ResultadoPautaEvent event = eventCaptor.getValue();
        assertThat(event.getPautaId()).isEqualTo(1L);
        assertThat(event.getTotalSim()).isEqualTo(7);
        assertThat(event.getTotalNao()).isEqualTo(3);
        assertThat(event.getResultado()).isEqualTo("APROVADA");

        verify(redisTemplate).delete(anyList());
    }

    @Test
    @DisplayName("Deve usar fallback do banco quando contadores Redis são zero")
    void deveUsarFallbackDoBancoQuandoRedisZerado() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.multiGet(anyList())).thenReturn(List.of("0", "0"));
        when(votoRepository.countByPautaIdAndValor(1L, VotoValor.SIM)).thenReturn(4L);
        when(votoRepository.countByPautaIdAndValor(1L, VotoValor.NAO)).thenReturn(4L);
        when(sessaoVotacaoRepository.save(any(SessaoVotacao.class))).thenReturn(sessao);
        doNothing().when(resultadoPautaProducer).publicar(any());

        sessaoEncerramentoService.encerrar(sessao);

        assertThat(sessao.getTotalSim()).isEqualTo(4);
        assertThat(sessao.getTotalNao()).isEqualTo(4);

        verify(resultadoPautaProducer).publicar(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getResultado()).isEqualTo("EMPATE");
    }

    @Test
    @DisplayName("Deve usar fallback do banco quando Redis falha")
    void deveUsarFallbackDoBancoQuandoRedisFalha() {
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis unavailable"));
        when(votoRepository.countByPautaIdAndValor(1L, VotoValor.SIM)).thenReturn(2L);
        when(votoRepository.countByPautaIdAndValor(1L, VotoValor.NAO)).thenReturn(5L);
        when(sessaoVotacaoRepository.save(any(SessaoVotacao.class))).thenReturn(sessao);
        doNothing().when(resultadoPautaProducer).publicar(any());

        sessaoEncerramentoService.encerrar(sessao);

        assertThat(sessao.getStatus()).isEqualTo(SessaoStatus.ENCERRADA);
        assertThat(sessao.getTotalSim()).isEqualTo(2);
        assertThat(sessao.getTotalNao()).isEqualTo(5);

        verify(resultadoPautaProducer).publicar(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getResultado()).isEqualTo("REPROVADA");
    }

    @Test
    @DisplayName("Deve publicar evento Kafka ao encerrar sessão")
    void devePublicarEventoKafkaAoEncerrar() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.multiGet(anyList())).thenReturn(List.of("10", "0"));
        when(sessaoVotacaoRepository.save(any(SessaoVotacao.class))).thenReturn(sessao);
        doNothing().when(resultadoPautaProducer).publicar(any());

        sessaoEncerramentoService.encerrar(sessao);

        verify(resultadoPautaProducer, times(1)).publicar(eventCaptor.capture());

        ResultadoPautaEvent event = eventCaptor.getValue();
        assertThat(event.getPautaId()).isEqualTo(1L);
        assertThat(event.getSessaoId()).isEqualTo(10L);
        assertThat(event.getResultado()).isEqualTo("APROVADA");
        assertThat(event.getDataEncerramentoSessao()).isEqualTo(sessao.getFim());
    }

    @Test
    @DisplayName("Deve continuar mesmo quando falha ao limpar Redis")
    void deveContinuarQuandoFalhaAoLimparRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.multiGet(anyList())).thenReturn(List.of("3", "2"));
        when(sessaoVotacaoRepository.save(any(SessaoVotacao.class))).thenReturn(sessao);
        doNothing().when(resultadoPautaProducer).publicar(any());
        when(redisTemplate.delete(anyList())).thenThrow(new RuntimeException("Redis delete failed"));

        sessaoEncerramentoService.encerrar(sessao);

        assertThat(sessao.getStatus()).isEqualTo(SessaoStatus.ENCERRADA);
        verify(sessaoVotacaoRepository).save(sessao);
        verify(resultadoPautaProducer).publicar(any());
    }
}
