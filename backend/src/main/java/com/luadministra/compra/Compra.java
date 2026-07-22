package com.luadministra.compra;

import com.luadministra.materiaprima.MateriaPrima;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.eclipse.jdt.annotation.Nullable;
import java.time.LocalDate;

@Entity
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private MateriaPrima materiaPrima;

    @NotNull
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull
    @Column(nullable = false)
    private Double cantidad;

    @NotNull
    @Column(nullable = false)
    private Double precio;

    private @Nullable String lugar;
    private @Nullable String url;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MateriaPrima getMateriaPrima() { return materiaPrima; }
    public void setMateriaPrima(MateriaPrima materiaPrima) { this.materiaPrima = materiaPrima; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public @Nullable String getLugar() { return lugar; }
    public void setLugar(@Nullable String lugar) { this.lugar = lugar; }

    public @Nullable String getUrl() { return url; }
    public void setUrl(@Nullable String url) { this.url = url; }
}
