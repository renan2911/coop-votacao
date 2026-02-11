package br.com.coop.votacao.repository;

import br.com.coop.votacao.entity.Pauta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PautaRepository extends JpaRepository<Pauta, Long> {
}
