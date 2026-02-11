package br.com.coop.votacao.service;

import br.com.coop.votacao.domain.SessaoStatus;
import br.com.coop.votacao.entity.Pauta;
import br.com.coop.votacao.entity.SessaoVotacao;
import br.com.coop.votacao.repository.SessaoVotacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - SessaoVotacaoScheduler")
class SessaoVotacaoSchedulerTest {

    @Mock
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Mock
    private SessaoEncerramentoService sessaoEncerramentoService;

    @InjectMocks
    private SessaoVotacaoScheduler sessaoVotacaoScheduler;

    @Test
    @DisplayName("Deve encerrar sessões expiradas")
    void deveEncerrarSessoesExpiradas() {
        Pauta pauta = new Pauta();
        pauta.setId(1L);

        SessaoVotacao sessaoExpirada = new SessaoVotacao();
        sessaoExpirada.setId(10L);
        sessaoExpirada.setPauta(pauta);
        sessaoExpirada.setInicio(Instant.now().minusSeconds(1200));
        sessaoExpirada.setFim(Instant.now().minusSeconds(600));
        sessaoExpirada.setStatus(SessaoStatus.ABERTA);

        when(sessaoVotacaoRepository.findByStatusAndFimLessThanEqual(eq(SessaoStatus.ABERTA), any(Instant.class)))
                .thenReturn(List.of(sessaoExpirada));

        sessaoVotacaoScheduler.encerrarSessoesExpiradas();

        verify(sessaoEncerramentoService, times(1)).encerrar(sessaoExpirada);
    }

    @Test
    @DisplayName("Não deve encerrar quando não há sessões expiradas")
    void naoDeveEncerrarQuandoNaoHaSessoesExpiradas() {
        when(sessaoVotacaoRepository.findByStatusAndFimLessThanEqual(eq(SessaoStatus.ABERTA), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        sessaoVotacaoScheduler.encerrarSessoesExpiradas();

        verify(sessaoEncerramentoService, never()).encerrar(any());
    }

    @Test
    @DisplayName("Deve continuar encerrando demais sessões mesmo com erro em uma")
    void deveContinuarEncerrandoMesmoComErro() {
        Pauta pauta = new Pauta();
        pauta.setId(1L);

        SessaoVotacao sessao1 = new SessaoVotacao();
        sessao1.setId(10L);
        sessao1.setPauta(pauta);
        sessao1.setStatus(SessaoStatus.ABERTA);

        SessaoVotacao sessao2 = new SessaoVotacao();
        sessao2.setId(20L);
        sessao2.setPauta(pauta);
        sessao2.setStatus(SessaoStatus.ABERTA);

        when(sessaoVotacaoRepository.findByStatusAndFimLessThanEqual(eq(SessaoStatus.ABERTA), any(Instant.class)))
                .thenReturn(List.of(sessao1, sessao2));
        doThrow(new RuntimeException("Erro inesperado")).when(sessaoEncerramentoService).encerrar(sessao1);
        doNothing().when(sessaoEncerramentoService).encerrar(sessao2);

        sessaoVotacaoScheduler.encerrarSessoesExpiradas();

        verify(sessaoEncerramentoService).encerrar(sessao1);
        verify(sessaoEncerramentoService).encerrar(sessao2);
    }
}
