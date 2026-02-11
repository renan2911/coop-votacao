package br.com.coop.votacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Testes de Contexto da Aplicação")
class CoopVotacaoApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	@DisplayName("Deve carregar o contexto da aplicação com sucesso")
	void contextLoads() {
		assertThat(applicationContext).isNotNull();
	}

}
