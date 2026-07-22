package com.luadministra.despacho;

import com.luadministra.colaboradora.Colaboradora;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Despacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Colaboradora colaboradora;

    @NotNull
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDespacho estado = EstadoDespacho.PENDIENTE;

    @OneToMany(mappedBy = "despacho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaDespacho> lineas = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Colaboradora getColaboradora() { return colaboradora; }
    public void setColaboradora(Colaboradora colaboradora) { this.colaboradora = colaboradora; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public EstadoDespacho getEstado() { return estado; }
    public void setEstado(EstadoDespacho estado) { this.estado = estado; }

    public List<LineaDespacho> getLineas() { return lineas; }
    public void setLineas(List<LineaDespacho> lineas) { this.lineas = lineas; }
}
