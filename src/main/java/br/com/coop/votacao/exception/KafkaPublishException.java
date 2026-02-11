package br.com.coop.votacao.exception;

public class KafkaPublishException extends RuntimeException {

    private final Long pautaId;

    public KafkaPublishException(Long pautaId, Throwable cause) {
        super("Falha ao publicar resultado da pauta " + pautaId + " no Kafka", cause);
        this.pautaId = pautaId;
    }

    public Long getPautaId() {
        return pautaId;
    }
}
