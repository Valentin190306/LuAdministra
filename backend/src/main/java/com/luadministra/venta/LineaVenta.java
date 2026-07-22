package com.luadministra.venta;

import com.luadministra.productoterminado.ProductoTerminado;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class LineaVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Venta venta;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductoTerminado productoTerminado;

    @NotNull
    @Column(nullable = false)
    private Double cantidad;

    @NotNull
    @Column(nullable = false)
    private Double precioUnitario;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }

    public ProductoTerminado getProductoTerminado() { return productoTerminado; }
    public void setProductoTerminado(ProductoTerminado productoTerminado) { this.productoTerminado = productoTerminado; }

    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }

    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
}
