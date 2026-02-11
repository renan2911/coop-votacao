package br.com.coop.votacao.service;

import br.com.coop.votacao.entity.Pauta;
import br.com.coop.votacao.domain.SessaoStatus;
import br.com.coop.votacao.entity.SessaoVotacao;
import br.com.coop.votacao.exception.BusinessException;
import br.com.coop.votacao.exception.NotFoundException;
import br.com.coop.votacao.repository.SessaoVotacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - SessaoVotacaoService")
class SessaoVotacaoServiceTest {

    @Mock
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Mock
    private PautaService pautaService;

    @InjectMocks
    private SessaoVotacaoService sessaoVotacaoService;

    private Pauta pauta;
    private SessaoVotacao sessao;

    @BeforeEach
    void setUp() {
        pauta = new Pauta();
        pauta.setId(1L);
        pauta.setTitulo("Pauta Teste");

        sessao = new SessaoVotacao();
        sessao.setId(1L);
        sessao.setPauta(pauta);
        sessao.setInicio(Instant.now());
        sessao.setFim(Instant.now().plusSeconds(600));
        sessao.setStatus(SessaoStatus.ABERTA);
    }

    @Test
    @DisplayName("Deve abrir sessão com sucesso usando duração padrão")
    void deveAbrirSessaoComDuracaoPadrao() {
        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(sessaoVotacaoRepository.findByPautaIdAndStatus(1L, SessaoStatus.ABERTA))
                .thenReturn(Optional.empty());
        when(sessaoVotacaoRepository.save(any(SessaoVotacao.class))).thenReturn(sessao);

        SessaoVotacao resultado = sessaoVotacaoService.abrirSessao(1L, null);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(SessaoStatus.ABERTA);
        verify(sessaoVotacaoRepository, times(1)).save(any(SessaoVotacao.class));
    }

    @Test
    @DisplayName("Deve abrir sessão com duração customizada")
    void deveAbrirSessaoComDuracaoCustomizada() {
        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(sessaoVotacaoRepository.findByPautaIdAndStatus(1L, SessaoStatus.ABERTA))
                .thenReturn(Optional.empty());
        when(sessaoVotacaoRepository.save(any(SessaoVotacao.class))).thenReturn(sessao);

        SessaoVotacao resultado = sessaoVotacaoService.abrirSessao(1L, 120L);

        assertThat(resultado).isNotNull();
        verify(sessaoVotacaoRepository, times(1)).save(any(SessaoVotacao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar abrir sessão duplicada")
    void deveLancarExcecaoAoAbrirSessaoDuplicada() {
        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(sessaoVotacaoRepository.findByPautaIdAndStatus(1L, SessaoStatus.ABERTA))
                .thenReturn(Optional.of(sessao));

        assertThatThrownBy(() -> sessaoVotacaoService.abrirSessao(1L, 60L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Já existe sessão de votação aberta");

        verify(sessaoVotacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar sessão aberta por pauta")
    void deveBuscarSessaoAbertaPorPauta() {
        when(sessaoVotacaoRepository.findByPautaIdAndStatus(1L, SessaoStatus.ABERTA))
                .thenReturn(Optional.of(sessao));

        SessaoVotacao resultado = sessaoVotacaoService.buscarSessaoAbertaPorPauta(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getStatus()).isEqualTo(SessaoStatus.ABERTA);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar sessão aberta inexistente")
    void deveLancarExcecaoAoBuscarSessaoAbertaInexistente() {
        when(sessaoVotacaoRepository.findByPautaIdAndStatus(1L, SessaoStatus.ABERTA))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessaoVotacaoService.buscarSessaoAbertaPorPauta(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Não há sessão de votação aberta");
    }

    @Test
    @DisplayName("Deve buscar sessão por ID")
    void deveBuscarSessaoPorId() {
        when(sessaoVotacaoRepository.findById(1L)).thenReturn(Optional.of(sessao));

        SessaoVotacao resultado = sessaoVotacaoService.buscarPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar sessão por ID inexistente")
    void deveLancarExcecaoAoBuscarSessaoPorIdInexistente() {
        when(sessaoVotacaoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessaoVotacaoService.buscarPorId(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Sessão de votação não encontrada");
    }

    @Test
    @DisplayName("Deve retornar sessão encerrada ao consultar resultado")
    void deveRetornarSessaoEncerradaAoConsultarResultado() {
        sessao.setStatus(SessaoStatus.ENCERRADA);
        when(sessaoVotacaoRepository.findByPautaIdAndStatus(1L, SessaoStatus.ENCERRADA))
                .thenReturn(Optional.of(sessao));

        SessaoVotacao resultado = sessaoVotacaoService.resultado(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(SessaoStatus.ENCERRADA);
        verify(sessaoVotacaoRepository, never()).findByPautaIdAndStatus(1L, SessaoStatus.ABERTA);
    }

    @Test
    @DisplayName("Deve retornar sessão aberta quando não há encerrada ao consultar resultado")
    void deveRetornarSessaoAbertaQuandoNaoHaEncerrada() {
        when(sessaoVotacaoRepository.findByPautaIdAndStatus(1L, SessaoStatus.ENCERRADA))
                .thenReturn(Optional.empty());
        when(sessaoVotacaoRepository.findByPautaIdAndStatus(1L, SessaoStatus.ABERTA))
                .thenReturn(Optional.of(sessao));

        SessaoVotacao resultado = sessaoVotacaoService.resultado(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(SessaoStatus.ABERTA);
    }

    @Test
    @DisplayName("Deve lançar exceção quando nenhuma sessão encontrada ao consultar resultado")
    void deveLancarExcecaoQuandoNenhumaSessaoEncontrada() {
        when(sessaoVotacaoRepository.findByPautaIdAndStatus(1L, SessaoStatus.ENCERRADA))
                .thenReturn(Optional.empty());
        when(sessaoVotacaoRepository.findByPautaIdAndStatus(1L, SessaoStatus.ABERTA))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessaoVotacaoService.resultado(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Nenhuma sessão de votação encontrada");
    }

    @Test
    @DisplayName("Deve lançar exceção quando sessão não está aberta no momento atual")
    void deveLancarExcecaoQuandoSessaoNaoAbertaNoMomento() {
        sessao.setFim(Instant.now().minusSeconds(60));
        when(sessaoVotacaoRepository.findByPautaIdAndStatus(1L, SessaoStatus.ABERTA))
                .thenReturn(Optional.of(sessao));

        assertThatThrownBy(() -> sessaoVotacaoService.buscarSessaoAbertaPorPauta(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não está aberta neste momento");
    }
}
