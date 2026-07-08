package com.luadministra.produccion;

import com.luadministra.productoterminado.ProductoTerminado;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
public class Produccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductoTerminado productoTerminado;

    @NotNull
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull
    @Column(nullable = false)
    private Double cantidadFabricada;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProductoTerminado getProductoTerminado() { return productoTerminado; }
    public void setProductoTerminado(ProductoTerminado productoTerminado) { this.productoTerminado = productoTerminado; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Double getCantidadFabricada() { return cantidadFabricada; }
    public void setCantidadFabricada(Double cantidadFabricada) { this.cantidadFabricada = cantidadFabricada; }
}
