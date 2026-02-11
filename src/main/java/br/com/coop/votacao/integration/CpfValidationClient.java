package br.com.coop.votacao.integration;

import br.com.coop.votacao.config.ApiProperties;
import br.com.coop.votacao.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import static br.com.coop.votacao.integration.CpfStatus.ABLE_TO_VOTE;

@Component
public class CpfValidationClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(CpfValidationClient.class);

    private final RestClient restClient;
    private final String baseUrl;

    public CpfValidationClient(RestClient cpfRestClient, ApiProperties apiProperties) {
        this.restClient = cpfRestClient;
        this.baseUrl = apiProperties.cpfValidationUrl();
    }

    @Cacheable(value = "cpf-status", key = "#cpf")
    public CpfStatus validarCpf(String cpf) {
        LOGGER.debug("Consultando serviço externo para CPF {}", cpf);

        try {
            CpfValidationResponse response = restClient.get()
                    .uri(baseUrl + "/" + cpf)
                    .retrieve()
                    .body(CpfValidationResponse.class);

            if (response == null || response.getStatus() == null) {
                throw new BusinessException("Resposta inválida do serviço de validação de CPF");
            }

            return response.getStatus();

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new BusinessException("CPF inválido");
            }
            LOGGER.error("Erro ao validar CPF {}: {}", cpf, e.getMessage());
            throw new BusinessException("Falha ao validar CPF no serviço externo");
        }
    }
}
