package br.com.coop.votacao.api.controller.v1;


import br.com.coop.votacao.api.dto.PautaRequest;
import br.com.coop.votacao.api.dto.PautaResponse;
import br.com.coop.votacao.entity.Pauta;
import br.com.coop.votacao.service.PautaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pautas")
@Tag(name = "Pautas")
public class PautaController {
    private final PautaService pautaService;

    public PautaController(PautaService pautaService) {
        this.pautaService = pautaService;
    }

    @Operation(summary = "Cria uma nova pauta")
    @PostMapping
    public ResponseEntity<PautaResponse> criar(@Valid @RequestBody PautaRequest request) {
        Pauta pauta = new Pauta();
        pauta.setTitulo(request.getTitulo());
        pauta.setDescricao(request.getDescricao());

        Pauta criada = pautaService.criar(pauta);

        PautaResponse response = toResponse(criada);

        return ResponseEntity
                .created(URI.create("/api/v1/pautas/" + criada.getId()))
                .body(response);
    }

    @Operation(summary = "Lista todas as pautas")
    @GetMapping
    public List<PautaResponse> listar() {
        return pautaService.listar().stream()
                .map(this::toResponse)
                .toList();
    }

    @Operation(summary = "Busca pauta por ID")
    @GetMapping("/{id}")
    public PautaResponse buscarPorId(@PathVariable Long id) {
        Pauta pauta = pautaService.buscarPorId(id);
        return toResponse(pauta);
    }

    private PautaResponse toResponse(Pauta pauta) {
        PautaResponse dto = new PautaResponse();
        dto.setId(pauta.getId());
        dto.setTitulo(pauta.getTitulo());
        dto.setDescricao(pauta.getDescricao());
        dto.setDataCriacao(pauta.getDataCriacao());
        return dto;
    }
}
