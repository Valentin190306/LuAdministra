package com.luadministra.rendicion;

import com.luadministra.consignacion.Consignacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Rendicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consignacion_id")
    private Consignacion consignacion;

    @NotNull
    @Column(nullable = false)
    private Double montoEntregado;

    @NotNull
    @Column(nullable = false)
    private LocalDate fecha;

    @OneToMany(mappedBy = "rendicion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaRendicion> lineas = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Consignacion getConsignacion() { return consignacion; }
    public void setConsignacion(Consignacion consignacion) { this.consignacion = consignacion; }

    public Double getMontoEntregado() { return montoEntregado; }
    public void setMontoEntregado(Double montoEntregado) { this.montoEntregado = montoEntregado; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public List<LineaRendicion> getLineas() { return lineas; }
    public void setLineas(List<LineaRendicion> lineas) { this.lineas = lineas; }
}
