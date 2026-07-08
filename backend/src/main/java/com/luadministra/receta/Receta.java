package com.luadministra.receta;

import com.luadministra.productoterminado.ProductoTerminado;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    private ProductoTerminado productoTerminado;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecetaDetalle> detalles = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProductoTerminado getProductoTerminado() { return productoTerminado; }
    public void setProductoTerminado(ProductoTerminado productoTerminado) { this.productoTerminado = productoTerminado; }

    public List<RecetaDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<RecetaDetalle> detalles) { this.detalles = detalles; }
}
