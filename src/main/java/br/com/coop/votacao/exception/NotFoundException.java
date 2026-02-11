package br.com.coop.votacao.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.time.Instant;

public class NotFoundException extends ErrorResponseException {

    public NotFoundException(String detail) {
        super(HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, detail), null);
        getBody().setTitle("Recurso não encontrado");
        getBody().setProperty("timestamp", Instant.now());
        getBody().setProperty("message", detail);
    }
}
