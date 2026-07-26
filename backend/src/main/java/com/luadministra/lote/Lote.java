package com.luadministra.lote;

import com.luadministra.producto.Producto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.eclipse.jdt.annotation.Nullable;
import java.time.LocalDate;

@Entity
@Table(name = "lote")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotNull
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull
    @Column(name = "cantidad_fabricada", nullable = false)
    private Double cantidadFabricada;

    @Column(name = "dias_vigencia")
    private @Nullable Integer diasVigencia;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Double getCantidadFabricada() { return cantidadFabricada; }
    public void setCantidadFabricada(Double cantidadFabricada) { this.cantidadFabricada = cantidadFabricada; }

    public @Nullable Integer getDiasVigencia() { return diasVigencia; }
    public void setDiasVigencia(@Nullable Integer diasVigencia) { this.diasVigencia = diasVigencia; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lote other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
