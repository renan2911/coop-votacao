package br.com.coop.votacao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api")
public record ApiProperties(
        String cpfValidationUrl,
        String votingResultTopic
) {
}
