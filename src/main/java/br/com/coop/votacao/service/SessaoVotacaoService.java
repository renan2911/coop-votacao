package br.com.coop.votacao.service;

import br.com.coop.votacao.entity.Pauta;
import br.com.coop.votacao.domain.SessaoStatus;
import br.com.coop.votacao.entity.SessaoVotacao;
import br.com.coop.votacao.exception.BusinessException;
import br.com.coop.votacao.exception.NotFoundException;
import br.com.coop.votacao.repository.SessaoVotacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class SessaoVotacaoService {

    private static final long DURACAO_PADRAO_SEGUNDOS = 60L;

    private final SessaoVotacaoRepository sessaoVotacaoRepository;
    private final PautaService pautaService;

    public SessaoVotacaoService(SessaoVotacaoRepository sessaoVotacaoRepository, PautaService pautaService) {
        this.sessaoVotacaoRepository = sessaoVotacaoRepository;
        this.pautaService = pautaService;
    }

    @Transactional
    public SessaoVotacao abrirSessao(Long pautaId, Long duracaoSegundos) {
        Pauta pauta = pautaService.buscarPorId(pautaId);

        sessaoVotacaoRepository.findByPautaIdAndStatus(pautaId, SessaoStatus.ABERTA)
                .ifPresent(s -> {
                    throw new BusinessException("Já existe sessão de votação aberta para a pauta " + pautaId);
                });

        long duracao = (duracaoSegundos == null || duracaoSegundos <= 0)
                ? DURACAO_PADRAO_SEGUNDOS
                : duracaoSegundos;

        Instant inicio = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant fim = inicio.plusSeconds(duracao);

        SessaoVotacao sessao = new SessaoVotacao();
        sessao.setPauta(pauta);
        sessao.setInicio(inicio);
        sessao.setFim(fim);
        sessao.setStatus(SessaoStatus.ABERTA);

        return sessaoVotacaoRepository.save(sessao);
    }

    @Transactional(readOnly = true)
    public SessaoVotacao buscarSessaoAbertaPorPauta(Long pautaId) {
        SessaoVotacao sessao = sessaoVotacaoRepository
                .findByPautaIdAndStatus(pautaId, SessaoStatus.ABERTA)
                .orElseThrow(() -> new NotFoundException("Não há sessão de votação aberta para a pauta " + pautaId));

        if (!sessao.isAbertaEm(Instant.now())) {
            throw new BusinessException("Sessão de votação da pauta " + pautaId + " não está aberta neste momento");
        }

        return sessao;
    }

    @Transactional(readOnly = true)
    public SessaoVotacao buscarPorId(Long id) {
        return sessaoVotacaoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sessão de votação não encontrada: " + id));
    }

    public SessaoVotacao resultado(Long pautaId){
        return sessaoVotacaoRepository
                .findByPautaIdAndStatus(pautaId, SessaoStatus.ENCERRADA)
                .or(() -> sessaoVotacaoRepository.findByPautaIdAndStatus(pautaId, SessaoStatus.ABERTA))
                .orElseThrow(() -> new NotFoundException("Nenhuma sessão de votação encontrada para a pauta " + pautaId));
    }

}
