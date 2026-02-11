package br.com.coop.votacao.service;

import br.com.coop.votacao.entity.Pauta;
import br.com.coop.votacao.entity.SessaoVotacao;
import br.com.coop.votacao.entity.Voto;
import br.com.coop.votacao.domain.VotoValor;
import br.com.coop.votacao.exception.BusinessException;
import br.com.coop.votacao.repository.VotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class VotoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VotoService.class);
    private static final String KEY_PREFIX = "votacao:sessao:";
    private static final int CONTADOR_TTL_HORAS = 48;
    
    private final VotoRepository votoRepository;
    private final PautaService pautaService;
    private final SessaoVotacaoService sessaoVotacaoService;
    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> incrementWithTtlScript;

    public VotoService(VotoRepository votoRepository,
                       PautaService pautaService,
                       SessaoVotacaoService sessaoVotacaoService,
                       StringRedisTemplate redisTemplate,
                       DefaultRedisScript<Long> incrementWithTtlScript) {
        this.votoRepository = votoRepository;
        this.pautaService = pautaService;
        this.sessaoVotacaoService = sessaoVotacaoService;
        this.redisTemplate = redisTemplate;
        this.incrementWithTtlScript = incrementWithTtlScript;
    }

    @Transactional
    public Voto registrarVoto(Long pautaId, String associadoId, VotoValor valor) {
        Pauta pauta = pautaService.buscarPorId(pautaId);
        SessaoVotacao sessaoAberta = sessaoVotacaoService.buscarSessaoAbertaPorPauta(pautaId);

        if (!sessaoAberta.isAbertaEm(Instant.now())) {
            throw new BusinessException("Sessão de votação da pauta " + pautaId + " está encerrada");
        }

//        CpfStatus status = cpfValidationClient.validarCpf(associadoId);
//        if (status == CpfStatus.UNABLE_TO_VOTE) {
//            throw new BusinessException("Associado não está habilitado para votar");
//        }
        
        Voto voto = new Voto();
        voto.setPauta(pauta);
        voto.setAssociadoId(associadoId);
        voto.setValor(valor);

        try {
            Voto salvo = votoRepository.saveAndFlush(voto);

            String chaveContador = valor == VotoValor.SIM ? 
                KEY_PREFIX + sessaoAberta.getId() + ":sim" : 
                KEY_PREFIX + sessaoAberta.getId() + ":nao";
            
            try {
                Long novoValor = redisTemplate.execute(
                    incrementWithTtlScript,
                    java.util.Collections.singletonList(chaveContador),
                    String.valueOf(CONTADOR_TTL_HORAS * 3600)
                );
                
                LOGGER.debug("Contador {} atualizado para {}", chaveContador, novoValor);
                
            } catch (Exception redisEx) {
                LOGGER.error("Erro ao atualizar contador no Redis para sessão {}: {}", 
                    sessaoAberta.getId(), redisEx.getMessage(), redisEx);
            }

            return salvo;
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Associado " + associadoId + " já votou na pauta " + pautaId);
        }
    }
}
