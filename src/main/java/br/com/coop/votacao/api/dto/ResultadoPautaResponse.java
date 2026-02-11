package br.com.coop.votacao.api.dto;

public class ResultadoPautaResponse {

    private Long pautaId;
    private Long sessaoId;
    private long totalSim;
    private long totalNao;
    private String resultado;

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
}
