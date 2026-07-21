package com.luadministra.productoterminado;

import com.luadministra.categoriaproductoterminado.CategoriaProductoTerminado;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.jdt.annotation.Nullable;

@Entity
public class ProductoTerminado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @NotNull
    @Column(nullable = false)
    private Double precioVenta;

    @NotNull
    @Column(nullable = false)
    private Double stockActual = 0.0;

    private @Nullable Double stockMinimo;

    @ManyToOne(fetch = FetchType.LAZY)
    private @Nullable CategoriaProductoTerminado categoria;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(Double precioVenta) { this.precioVenta = precioVenta; }

    public Double getStockActual() { return stockActual; }
    public void setStockActual(Double stockActual) { this.stockActual = stockActual; }

    public @Nullable Double getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(@Nullable Double stockMinimo) { this.stockMinimo = stockMinimo; }

    public @Nullable CategoriaProductoTerminado getCategoria() { return categoria; }
    public void setCategoria(@Nullable CategoriaProductoTerminado categoria) { this.categoria = categoria; }
}
