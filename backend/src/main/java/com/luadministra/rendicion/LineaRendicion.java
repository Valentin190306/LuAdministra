package com.luadministra.rendicion;

import com.luadministra.productoterminado.ProductoTerminado;
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
    private ProductoTerminado productoTerminado;

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

    public ProductoTerminado getProductoTerminado() { return productoTerminado; }
    public void setProductoTerminado(ProductoTerminado productoTerminado) { this.productoTerminado = productoTerminado; }

    public Double getCantidadVendida() { return cantidadVendida; }
    public void setCantidadVendida(Double cantidadVendida) { this.cantidadVendida = cantidadVendida; }

    public Double getCantidadDevuelta() { return cantidadDevuelta; }
    public void setCantidadDevuelta(Double cantidadDevuelta) { this.cantidadDevuelta = cantidadDevuelta; }
}
