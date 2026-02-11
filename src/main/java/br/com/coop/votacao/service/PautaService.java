package br.com.coop.votacao.service;

import br.com.coop.votacao.entity.Pauta;
import br.com.coop.votacao.entity.SessaoVotacao;
import br.com.coop.votacao.exception.NotFoundException;
import br.com.coop.votacao.messaging.ResultadoPautaEvent;
import br.com.coop.votacao.repository.PautaRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PautaService {

    private final PautaRepository pautaRepository;

    public PautaService(PautaRepository pautaRepository) {
        this.pautaRepository = pautaRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Pauta criar(Pauta pauta) {
        return pautaRepository.save(pauta);
    }

    @Cacheable(value = "pautas", key = "#id")
    @Transactional(readOnly = true)
    public Pauta buscarPorId(Long id) {
        return pautaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pauta não encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<Pauta> listar() {
        return pautaRepository.findAll();
    }

    @NonNull
    public static ResultadoPautaEvent getResultadoPautaEvent(SessaoVotacao sessao, long totalSim, long totalNao) {
        ResultadoPautaEvent event = new ResultadoPautaEvent();
        event.setPautaId(sessao.getPauta().getId());
        event.setSessaoId(sessao.getId());
        event.setTotalSim(totalSim);
        event.setTotalNao(totalNao);
        event.setDataEncerramentoSessao(sessao.getFim());

        if (totalSim > totalNao) {
            event.setResultado("APROVADA");
        } else if (totalSim < totalNao) {
            event.setResultado("REPROVADA");
        } else {
            event.setResultado("EMPATE");
        }
        return event;
    }
}
