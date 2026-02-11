package br.com.coop.votacao.repository;

import br.com.coop.votacao.entity.Pauta;
import br.com.coop.votacao.entity.Voto;
import br.com.coop.votacao.domain.VotoValor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Testes de Integração - VotoRepository")
class VotoRepositoryTest {

    @Autowired
    private VotoRepository votoRepository;

    @Autowired
    private PautaRepository pautaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Pauta pauta;

    @BeforeEach
    void setUp() {
        pauta = new Pauta();
        pauta.setTitulo("Pauta Teste");
        pauta = pautaRepository.save(pauta);
    }

    @AfterEach
    void tearDown() {
        entityManager.clear();
        votoRepository.deleteAll();
        pautaRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve salvar e buscar voto por ID")
    void deveSalvarEBuscarVotoPorId() {
        Voto voto = new Voto();
        voto.setPauta(pauta);
        voto.setAssociadoId("12345678901");
        voto.setValor(VotoValor.SIM);

        Voto salvo = votoRepository.save(voto);

        assertThat(salvo.getId()).isNotNull();
        assertThat(votoRepository.findById(salvo.getId())).isPresent();
    }

    @Test
    @DisplayName("Deve contar votos SIM por pauta")
    void deveContarVotosSimPorPauta() {
        for (int i = 0; i < 5; i++) {
            Voto voto = new Voto();
            voto.setPauta(pauta);
            voto.setAssociadoId("1234567890" + i);
            voto.setValor(VotoValor.SIM);
            votoRepository.save(voto);
        }

        for (int i = 5; i < 8; i++) {
            Voto voto = new Voto();
            voto.setPauta(pauta);
            voto.setAssociadoId("1234567890" + i);
            voto.setValor(VotoValor.NAO);
            votoRepository.save(voto);
        }

        long totalSim = votoRepository.countByPautaIdAndValor(pauta.getId(), VotoValor.SIM);
        long totalNao = votoRepository.countByPautaIdAndValor(pauta.getId(), VotoValor.NAO);

        assertThat(totalSim).isEqualTo(5);
        assertThat(totalNao).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve garantir unique constraint pauta + associado")
    void deveGarantirUniqueConstraintPautaAssociado() {
        Voto voto1 = new Voto();
        voto1.setPauta(pauta);
        voto1.setAssociadoId("12345678901");
        voto1.setValor(VotoValor.SIM);
        votoRepository.saveAndFlush(voto1);

        Voto voto2 = new Voto();
        voto2.setPauta(pauta);
        voto2.setAssociadoId("12345678901");
        voto2.setValor(VotoValor.NAO);

        assertThatThrownBy(() -> votoRepository.saveAndFlush(voto2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
