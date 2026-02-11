package br.com.coop.votacao.repository;

import br.com.coop.votacao.entity.Voto;
import br.com.coop.votacao.domain.VotoValor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotoRepository extends JpaRepository<Voto, Long> {
    long countByPautaIdAndValor(Long id, VotoValor votoValor);
}
