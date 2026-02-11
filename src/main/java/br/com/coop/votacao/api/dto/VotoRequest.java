package br.com.coop.votacao.api.dto;


import br.com.coop.votacao.domain.VotoValor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class VotoRequest {

    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos numéricos")
    private String cpf;

    @NotNull
    private VotoValor voto;

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public VotoValor getVoto() {
        return voto;
    }

    public void setVoto(VotoValor voto) {
        this.voto = voto;
    }
}
