package br.com.coop.votacao.messaging;

import br.com.coop.votacao.config.ApiProperties;
import br.com.coop.votacao.exception.KafkaPublishException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

@Component
public class ResultadoPautaProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultadoPautaProducer.class);
    private static final int SEND_TIMEOUT_SECONDS = 30;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public ResultadoPautaProducer(KafkaTemplate<String, Object> kafkaTemplate,
                                  ApiProperties apiProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = apiProperties.votingResultTopic();
    }

    public void publicar(ResultadoPautaEvent event) {
        String key = String.valueOf(event.getPautaId());

        try {
            SendResult<String, Object> result = kafkaTemplate.send(topic, key, event)
                    .orTimeout(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .join();

            LOGGER.info("Resultado da pauta {} publicado no tópico {} - partition: {}, offset: {}",
                    event.getPautaId(),
                    topic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

        } catch (CompletionException e) {
            LOGGER.error("Falha ao publicar resultado da pauta {} no Kafka", event.getPautaId(), e.getCause());
            throw new KafkaPublishException(event.getPautaId(), e.getCause());
        }
    }
}

