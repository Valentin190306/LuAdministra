package com.luadministra.materiaprima;

import com.luadministra.categoria.Categoria;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.jdt.annotation.Nullable;

@Entity
public class MateriaPrima {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @NotBlank
    @Column(nullable = false)
    private String unidadMedida;

    @NotNull
    @Column(nullable = false)
    private Double stockActual = 0.0;

    private @Nullable Double stockMinimo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private @Nullable Categoria categoria;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public Double getStockActual() { return stockActual; }
    public void setStockActual(Double stockActual) { this.stockActual = stockActual; }

    public @Nullable Double getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(@Nullable Double stockMinimo) { this.stockMinimo = stockMinimo; }

    public @Nullable Categoria getCategoria() { return categoria; }
    public void setCategoria(@Nullable Categoria categoria) { this.categoria = categoria; }
}
