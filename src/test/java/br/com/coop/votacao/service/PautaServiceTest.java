package br.com.coop.votacao.service;

import br.com.coop.votacao.entity.Pauta;
import br.com.coop.votacao.exception.NotFoundException;
import br.com.coop.votacao.repository.PautaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - PautaService")
class PautaServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    @InjectMocks
    private PautaService pautaService;

    private Pauta pautaExemplo;

    @BeforeEach
    void setUp() {
        pautaExemplo = new Pauta();
        pautaExemplo.setId(1L);
        pautaExemplo.setTitulo("Construção de Nova Sede");
        pautaExemplo.setDescricao("Aprovação para construção da nova sede administrativa");
        pautaExemplo.setDataCriacao(Instant.now());
    }

    @Test
    @DisplayName("Deve criar uma pauta com sucesso")
    void deveCriarPautaComSucesso() {
        when(pautaRepository.save(any(Pauta.class))).thenReturn(pautaExemplo);

        Pauta resultado = pautaService.criar(pautaExemplo);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTitulo()).isEqualTo("Construção de Nova Sede");
        verify(pautaRepository, times(1)).save(any(Pauta.class));
    }

    @Test
    @DisplayName("Deve buscar pauta por ID com sucesso")
    void deveBuscarPautaPorIdComSucesso() {
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pautaExemplo));

        Pauta resultado = pautaService.buscarPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTitulo()).isEqualTo("Construção de Nova Sede");
        verify(pautaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao buscar pauta inexistente")
    void deveLancarExcecaoAoBuscarPautaInexistente() {
        when(pautaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pautaService.buscarPorId(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Pauta não encontrada");
        
        verify(pautaRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Deve listar todas as pautas")
    void deveListarTodasAsPautas() {
        Pauta pauta2 = new Pauta();
        pauta2.setId(2L);
        pauta2.setTitulo("Aprovação de Orçamento");
        
        when(pautaRepository.findAll()).thenReturn(Arrays.asList(pautaExemplo, pauta2));

        List<Pauta> resultado = pautaService.listar();

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Pauta::getTitulo)
                .containsExactly("Construção de Nova Sede", "Aprovação de Orçamento");
        verify(pautaRepository, times(1)).findAll();
    }
}
