package br.com.coop.votacao.repository;

import br.com.coop.votacao.domain.SessaoStatus;
import br.com.coop.votacao.entity.SessaoVotacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SessaoVotacaoRepository extends JpaRepository<SessaoVotacao, Long> {
    Optional<SessaoVotacao> findByPautaIdAndStatus(Long pautaId, SessaoStatus status);

    List<SessaoVotacao> findByStatusAndFimLessThanEqual(SessaoStatus status, Instant fimMaximo);
}
