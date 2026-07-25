package com.luadministra.rendicion;

import com.luadministra.consignacion.LineaConsignacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class LineaRendicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Rendicion rendicion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linea_consignacion_id")
    private LineaConsignacion lineaConsignacion;

    @NotNull
    @Column(nullable = false)
    private Double cantidadVendida;

    @NotNull
    @Column(nullable = false)
    private Double cantidadDevuelta;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Rendicion getRendicion() { return rendicion; }
    public void setRendicion(Rendicion rendicion) { this.rendicion = rendicion; }

    public LineaConsignacion getLineaConsignacion() { return lineaConsignacion; }
    public void setLineaConsignacion(LineaConsignacion lineaConsignacion) { this.lineaConsignacion = lineaConsignacion; }

    public Double getCantidadVendida() { return cantidadVendida; }
    public void setCantidadVendida(Double cantidadVendida) { this.cantidadVendida = cantidadVendida; }

    public Double getCantidadDevuelta() { return cantidadDevuelta; }
    public void setCantidadDevuelta(Double cantidadDevuelta) { this.cantidadDevuelta = cantidadDevuelta; }
}
