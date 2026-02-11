package br.com.coop.votacao.service;

import br.com.coop.votacao.domain.SessaoStatus;
import br.com.coop.votacao.entity.SessaoVotacao;
import br.com.coop.votacao.repository.SessaoVotacaoRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@EnableScheduling
public class SessaoVotacaoScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessaoVotacaoScheduler.class);

    private final SessaoVotacaoRepository sessaoVotacaoRepository;
    private final SessaoEncerramentoService sessaoEncerramentoService;

    public SessaoVotacaoScheduler(SessaoVotacaoRepository sessaoVotacaoRepository,
                                  SessaoEncerramentoService sessaoEncerramentoService) {
        this.sessaoVotacaoRepository = sessaoVotacaoRepository;
        this.sessaoEncerramentoService = sessaoEncerramentoService;
    }

    @Scheduled(fixedDelay = 9_000)
    @SchedulerLock(
            name = "encerrarSessoesExpiradas",
            lockAtMostFor = "2m",
            lockAtLeastFor = "2s"
    )
    public void encerrarSessoesExpiradas() {
        Instant agora = Instant.now();
        List<SessaoVotacao> expiradas = sessaoVotacaoRepository
                .findByStatusAndFimLessThanEqual(SessaoStatus.ABERTA, agora);

        if (expiradas.isEmpty()) {
            return;
        }

        LOGGER.info("Encerrando {} sessões expiradas", expiradas.size());

        for (SessaoVotacao sessao : expiradas) {
            try {
                sessaoEncerramentoService.encerrar(sessao);
            } catch (Exception e) {
                LOGGER.error("Erro ao encerrar sessão {}: {}", sessao.getId(), e.getMessage(), e);
            }
        }
    }
}
