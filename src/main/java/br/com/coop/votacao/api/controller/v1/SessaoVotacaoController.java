package br.com.coop.votacao.api.controller.v1;

import br.com.coop.votacao.api.dto.ResultadoPautaResponse;
import br.com.coop.votacao.api.dto.SessaoVotacaoRequest;
import br.com.coop.votacao.api.dto.SessaoVotacaoResponse;
import br.com.coop.votacao.domain.SessaoStatus;
import br.com.coop.votacao.entity.SessaoVotacao;
import br.com.coop.votacao.exception.NotFoundException;
import br.com.coop.votacao.repository.SessaoVotacaoRepository;
import br.com.coop.votacao.service.SessaoVotacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/pautas")
@Tag(name = "Sessões de Votação")
public class SessaoVotacaoController {
    private final SessaoVotacaoService sessaoVotacaoService;

    public SessaoVotacaoController(SessaoVotacaoService sessaoVotacaoService) {
        this.sessaoVotacaoService = sessaoVotacaoService;
    }

    @Operation(summary = "Abre uma sessão de votação para a pauta")
    @PostMapping("/sessoes/{pautaId}")
    public ResponseEntity<SessaoVotacaoResponse> abrirSessao(@PathVariable Long pautaId,
                                                             @Valid @RequestBody(required = false) SessaoVotacaoRequest request) {
        Long duracao = request != null ? request.getDuracaoSegundos() : null;
        SessaoVotacao sessao = sessaoVotacaoService.abrirSessao(pautaId, duracao);
        SessaoVotacaoResponse response = toResponse(sessao);

        return ResponseEntity
                .created(URI.create("/api/v1/pautas/sessoes/" + pautaId + "/" + sessao.getId()))
                .body(response);
    }

    @Operation(summary = "Busca sessão de votação por ID")
    @GetMapping("/sessoes/{pautaId}/{sessaoId}")
    public SessaoVotacaoResponse buscarSessao(@PathVariable Long pautaId, @PathVariable Long sessaoId) {
        SessaoVotacao sessao = sessaoVotacaoService.buscarPorId(sessaoId);
        return toResponse(sessao);
    }

    @Operation(summary = "Obtém o resultado da votação da pauta")
    @GetMapping("/resultado/{pautaId}")
    public ResultadoPautaResponse resultado(@PathVariable Long pautaId) {
        SessaoVotacao sessao = sessaoVotacaoService.resultado(pautaId);

        ResultadoPautaResponse resp = new ResultadoPautaResponse();
        resp.setPautaId(pautaId);
        resp.setSessaoId(sessao.getId());
        resp.setTotalSim(sessao.getTotalSim());
        resp.setTotalNao(sessao.getTotalNao());
        resp.setResultado(calcularResultado(sessao.getTotalSim(), sessao.getTotalNao()));

        return resp;
    }

    private String calcularResultado(long totalSim, long totalNao) {
        if (totalSim > totalNao) return "APROVADA";
        if (totalSim < totalNao) return "REPROVADA";
        return "EMPATE";
    }

    private SessaoVotacaoResponse toResponse(SessaoVotacao sessao) {
        SessaoVotacaoResponse dto = new SessaoVotacaoResponse();
        dto.setId(sessao.getId());
        dto.setPautaId(sessao.getPauta().getId());
        dto.setInicio(sessao.getInicio());
        dto.setFim(sessao.getFim());
        dto.setStatus(sessao.getStatus());
        dto.setTotalSim(sessao.getTotalSim());
        dto.setTotalNao(sessao.getTotalNao());
        return dto;
    }
}
