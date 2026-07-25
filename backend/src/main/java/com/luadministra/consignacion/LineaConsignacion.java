package com.luadministra.consignacion;

import com.luadministra.producto.Producto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "linea_consignacion")
public class LineaConsignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consignacion_id", nullable = false)
    private Consignacion consignacion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotNull
    @Column(nullable = false)
    private Double cantidad;

    @NotNull
    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Consignacion getConsignacion() { return consignacion; }
    public void setConsignacion(Consignacion consignacion) { this.consignacion = consignacion; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }

    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
}
