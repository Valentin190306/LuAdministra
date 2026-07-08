package com.luadministra.receta;

import com.luadministra.materiaprima.MateriaPrima;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class RecetaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Receta receta;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private MateriaPrima materiaPrima;

    @NotNull
    @Column(nullable = false)
    private Double cantidad;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Receta getReceta() { return receta; }
    public void setReceta(Receta receta) { this.receta = receta; }

    public MateriaPrima getMateriaPrima() { return materiaPrima; }
    public void setMateriaPrima(MateriaPrima materiaPrima) { this.materiaPrima = materiaPrima; }

    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
}
