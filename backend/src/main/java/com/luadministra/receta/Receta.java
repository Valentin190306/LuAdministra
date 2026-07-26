package com.luadministra.receta;

import com.luadministra.producto.Producto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "receta")
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecetaDetalle> detalles = new ArrayList<>();

    private @Nullable String notas;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public List<RecetaDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<RecetaDetalle> detalles) { this.detalles = detalles; }

    public @Nullable String getNotas() { return notas; }
    public void setNotas(@Nullable String notas) { this.notas = notas; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Receta other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
