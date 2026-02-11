package br.com.coop.votacao.repository;

import br.com.coop.votacao.entity.Pauta;
import br.com.coop.votacao.domain.SessaoStatus;
import br.com.coop.votacao.entity.SessaoVotacao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Testes de Integração - SessaoVotacaoRepository")
class SessaoVotacaoRepositoryTest {

    @Autowired
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Autowired
    private PautaRepository pautaRepository;

    private Pauta pauta;

    @BeforeEach
    void setUp() {
        pauta = new Pauta();
        pauta.setTitulo("Pauta Teste");
        pauta.setDescricao("Descrição de teste");
        pauta = pautaRepository.save(pauta);
    }

    @AfterEach
    void tearDown() {
        sessaoVotacaoRepository.deleteAll();
        pautaRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve salvar e buscar sessão por ID")
    void deveSalvarEBuscarSessaoPorId() {
        SessaoVotacao sessao = new SessaoVotacao();
        sessao.setPauta(pauta);
        sessao.setInicio(Instant.now());
        sessao.setFim(Instant.now().plusSeconds(600));
        sessao.setStatus(SessaoStatus.ABERTA);

        SessaoVotacao salva = sessaoVotacaoRepository.save(sessao);

        assertThat(salva.getId()).isNotNull();
        
        Optional<SessaoVotacao> encontrada = sessaoVotacaoRepository.findById(salva.getId());
        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getPauta().getId()).isEqualTo(pauta.getId());
        assertThat(encontrada.get().getStatus()).isEqualTo(SessaoStatus.ABERTA);
    }

    @Test
    @DisplayName("Deve buscar sessão por pautaId e status")
    void deveBuscarSessaoPorPautaIdEStatus() {
        SessaoVotacao sessao = new SessaoVotacao();
        sessao.setPauta(pauta);
        sessao.setInicio(Instant.now());
        sessao.setFim(Instant.now().plusSeconds(600));
        sessao.setStatus(SessaoStatus.ABERTA);
        sessaoVotacaoRepository.save(sessao);

        Optional<SessaoVotacao> resultado = sessaoVotacaoRepository
                .findByPautaIdAndStatus(pauta.getId(), SessaoStatus.ABERTA);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getPauta().getId()).isEqualTo(pauta.getId());
        assertThat(resultado.get().getStatus()).isEqualTo(SessaoStatus.ABERTA);
    }

    @Test
    @DisplayName("Deve buscar sessões expiradas")
    void deveBuscarSessoesExpiradas() {
        Instant agora = Instant.now();
        
        SessaoVotacao expirada1 = new SessaoVotacao();
        expirada1.setPauta(pauta);
        expirada1.setInicio(agora.minusSeconds(1200));
        expirada1.setFim(agora.minusSeconds(600));
        expirada1.setStatus(SessaoStatus.ABERTA);
        sessaoVotacaoRepository.save(expirada1);

        SessaoVotacao ativa = new SessaoVotacao();
        ativa.setPauta(pauta);
        ativa.setInicio(agora);
        ativa.setFim(agora.plusSeconds(600));
        ativa.setStatus(SessaoStatus.ABERTA);
        sessaoVotacaoRepository.save(ativa);

        List<SessaoVotacao> expiradas = sessaoVotacaoRepository
                .findByStatusAndFimLessThanEqual(SessaoStatus.ABERTA, agora);

        assertThat(expiradas).hasSize(1);
        assertThat(expiradas.get(0).getId()).isEqualTo(expirada1.getId());
    }

    @Test
    @DisplayName("Deve atualizar contadores de votos")
    void deveAtualizarContadoresDeVotos() {
        SessaoVotacao sessao = new SessaoVotacao();
        sessao.setPauta(pauta);
        sessao.setInicio(Instant.now());
        sessao.setFim(Instant.now().plusSeconds(600));
        sessao.setStatus(SessaoStatus.ABERTA);
        sessao.setTotalSim(0);
        sessao.setTotalNao(0);
        sessao = sessaoVotacaoRepository.save(sessao);

        sessao.setTotalSim(10);
        sessao.setTotalNao(5);
        sessaoVotacaoRepository.save(sessao);

        SessaoVotacao atualizada = sessaoVotacaoRepository.findById(sessao.getId()).orElseThrow();
        assertThat(atualizada.getTotalSim()).isEqualTo(10);
        assertThat(atualizada.getTotalNao()).isEqualTo(5);
    }
}
