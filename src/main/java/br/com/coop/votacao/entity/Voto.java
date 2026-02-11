package br.com.coop.votacao.entity;

import br.com.coop.votacao.domain.VotoValor;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class Voto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pauta_id")
    private Pauta pauta;

    @Column(name = "associado_id", nullable = false, length = 11)
    private String associadoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private VotoValor valor;

    @Column(nullable = false)
    private Instant dataHora;

    @PrePersist
    public void prePersist() {
        if (dataHora == null) {
            dataHora = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pauta getPauta() {
        return pauta;
    }

    public void setPauta(Pauta pauta) {
        this.pauta = pauta;
    }

    public String getAssociadoId() {
        return associadoId;
    }

    public void setAssociadoId(String associadoId) {
        this.associadoId = associadoId;
    }

    public VotoValor getValor() {
        return valor;
    }

    public void setValor(VotoValor valor) {
        this.valor = valor;
    }

    public Instant getDataHora() {
        return dataHora;
    }

    public void setDataHora(Instant dataHora) {
        this.dataHora = dataHora;
    }
}
