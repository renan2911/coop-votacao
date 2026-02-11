package br.com.coop.votacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CoopVotacaoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoopVotacaoApplication.class, args);
	}

}
