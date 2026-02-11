package br.com.coop.votacao.api.dto;

public class SessaoVotacaoRequest {
    private Long duracaoSegundos;

    public Long getDuracaoSegundos() {
        return duracaoSegundos;
    }

    public void setDuracaoSegundos(Long duracaoSegundos) {
        this.duracaoSegundos = duracaoSegundos;
    }
}
