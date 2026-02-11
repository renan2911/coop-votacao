package br.com.coop.votacao.api.dto;



import br.com.coop.votacao.domain.SessaoStatus;

import java.time.Instant;

public class SessaoVotacaoResponse {
    private Long id;
    private Long pautaId;
    private Instant inicio;
    private Instant fim;
    private SessaoStatus status;
    private long totalSim;
    private long totalNao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPautaId() {
        return pautaId;
    }

    public void setPautaId(Long pautaId) {
        this.pautaId = pautaId;
    }

    public Instant getInicio() {
        return inicio;
    }

    public void setInicio(Instant inicio) {
        this.inicio = inicio;
    }

    public Instant getFim() {
        return fim;
    }

    public void setFim(Instant fim) {
        this.fim = fim;
    }

    public SessaoStatus getStatus() {
        return status;
    }

    public void setStatus(SessaoStatus status) {
        this.status = status;
    }

    public long getTotalSim() {
        return totalSim;
    }

    public void setTotalSim(long totalSim) {
        this.totalSim = totalSim;
    }

    public long getTotalNao() {
        return totalNao;
    }

    public void setTotalNao(long totalNao) {
        this.totalNao = totalNao;
    }
}
