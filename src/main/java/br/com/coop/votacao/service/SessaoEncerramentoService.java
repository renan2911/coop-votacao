package br.com.coop.votacao.service;

import br.com.coop.votacao.domain.SessaoStatus;
import br.com.coop.votacao.domain.VotoValor;
import br.com.coop.votacao.entity.SessaoVotacao;
import br.com.coop.votacao.messaging.ResultadoPautaEvent;
import br.com.coop.votacao.messaging.ResultadoPautaProducer;
import br.com.coop.votacao.repository.SessaoVotacaoRepository;
import br.com.coop.votacao.repository.VotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SessaoEncerramentoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessaoEncerramentoService.class);
    private static final String KEY_PREFIX = "votacao:sessao:";

    private final SessaoVotacaoRepository sessaoVotacaoRepository;
    private final VotoRepository votoRepository;
    private final StringRedisTemplate redisTemplate;
    private final ResultadoPautaProducer resultadoPautaProducer;

    public SessaoEncerramentoService(SessaoVotacaoRepository sessaoVotacaoRepository,
                                     VotoRepository votoRepository,
                                     StringRedisTemplate redisTemplate,
                                     ResultadoPautaProducer resultadoPautaProducer) {
        this.sessaoVotacaoRepository = sessaoVotacaoRepository;
        this.votoRepository = votoRepository;
        this.redisTemplate = redisTemplate;
        this.resultadoPautaProducer = resultadoPautaProducer;
    }

    @Transactional
    public void encerrar(SessaoVotacao sessao) {
        String simKey = KEY_PREFIX + sessao.getId() + ":sim";
        String naoKey = KEY_PREFIX + sessao.getId() + ":nao";

        long totalSim = 0L;
        long totalNao = 0L;

        try {
            List<String> valores = redisTemplate.opsForValue().multiGet(List.of(simKey, naoKey));

            if (valores != null) {
                totalSim = valores.get(0) != null ? Long.parseLong(valores.get(0)) : 0L;
                totalNao = valores.get(1) != null ? Long.parseLong(valores.get(1)) : 0L;
                LOGGER.info("Contador Redis: totalSim {} - totalNao {}", totalSim, totalNao);
            }

            if (totalSim == 0 && totalNao == 0) {
                LOGGER.warn("Contadores zerados para sessão {} - verificando DB", sessao.getId());
                totalSim = votoRepository.countByPautaIdAndValor(sessao.getPauta().getId(), VotoValor.SIM);
                totalNao = votoRepository.countByPautaIdAndValor(sessao.getPauta().getId(), VotoValor.NAO);
            }

        } catch (Exception redisEx) {
            LOGGER.error("Erro ao buscar contadores do Redis para sessão {}: {}",
                    sessao.getId(), redisEx.getMessage(), redisEx);

            totalSim = votoRepository.countByPautaIdAndValor(sessao.getPauta().getId(), VotoValor.SIM);
            totalNao = votoRepository.countByPautaIdAndValor(sessao.getPauta().getId(), VotoValor.NAO);
        }

        sessao.setStatus(SessaoStatus.ENCERRADA);
        sessao.setTotalSim(totalSim);
        sessao.setTotalNao(totalNao);

        sessaoVotacaoRepository.save(sessao);

        ResultadoPautaEvent event = PautaService.getResultadoPautaEvent(sessao, totalSim, totalNao);

        resultadoPautaProducer.publicar(event);

        try {
            redisTemplate.delete(List.of(simKey, naoKey));
            LOGGER.debug("Contadores da sessão {} removidos do Redis", sessao.getId());
        } catch (Exception redisEx) {
            LOGGER.warn("Erro ao remover contadores do Redis para sessão {}: {}",
                    sessao.getId(), redisEx.getMessage());
        }

        LOGGER.info("Sessão {} da pauta {} encerrada. Sim: {}, Não: {}, Resultado: {}",
                sessao.getId(), sessao.getPauta().getId(), totalSim, totalNao, event.getResultado());
    }
}
