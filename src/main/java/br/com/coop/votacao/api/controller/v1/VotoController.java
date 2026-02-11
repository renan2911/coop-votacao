package br.com.coop.votacao.api.controller.v1;


import br.com.coop.votacao.api.dto.VotoRequest;
import br.com.coop.votacao.entity.Voto;
import br.com.coop.votacao.service.VotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/pautas")
@Tag(name = "Votos")
public class VotoController {
    private final VotoService votoService;

    public VotoController(VotoService votoService) {
        this.votoService = votoService;
    }

    @Operation(summary = "Registra o voto de um associado em uma pauta")
    @PostMapping("/votos/{pautaId}")
    public ResponseEntity<Void> votar(@PathVariable Long pautaId,
                                      @Valid @RequestBody VotoRequest request) {
        Voto voto = votoService.registrarVoto(pautaId, request.getCpf(), request.getVoto());
        return ResponseEntity
                .created(URI.create("/api/v1/pautas/votos/" + pautaId + "/" + voto.getId()))
                .build();
    }
}
