package br.com.coop.votacao.messaging;

import java.time.Instant;

public class ResultadoPautaEvent {
    private Long pautaId;
    private Long sessaoId;
    private long totalSim;
    private long totalNao;
    private String resultado;
    private Instant dataEncerramentoSessao;
    private Instant dataPublicacao;

    public ResultadoPautaEvent() {
        this.dataPublicacao = Instant.now();
    }

    public Long getPautaId() {
        return pautaId;
    }

    public void setPautaId(Long pautaId) {
        this.pautaId = pautaId;
    }

    public Long getSessaoId() {
        return sessaoId;
    }

    public void setSessaoId(Long sessaoId) {
        this.sessaoId = sessaoId;
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

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public Instant getDataEncerramentoSessao() {
        return dataEncerramentoSessao;
    }

    public void setDataEncerramentoSessao(Instant dataEncerramentoSessao) {
        this.dataEncerramentoSessao = dataEncerramentoSessao;
    }

    public Instant getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(Instant dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }
}
