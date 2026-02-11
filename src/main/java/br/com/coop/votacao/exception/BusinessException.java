package br.com.coop.votacao.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.time.Instant;

public class BusinessException extends ErrorResponseException {

    public BusinessException(String detail) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, detail), null);
        getBody().setTitle("Erro de regra de negócio");
        getBody().setProperty("timestamp", Instant.now());
        getBody().setProperty("message", detail);
    }
}

