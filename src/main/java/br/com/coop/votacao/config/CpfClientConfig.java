package br.com.coop.votacao.config;

import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CpfClientConfig {

    @Bean
    public RestClient cpfRestClient(RestClientBuilderConfigurer configurer) {
        return configurer.configure(RestClient.builder()).build();
    }

}
