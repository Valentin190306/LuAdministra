package com.luadministra.venta;

import com.luadministra.consignacion.Consignacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.eclipse.jdt.annotation.Nullable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private LocalDate fecha;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaVenta> lineas = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consignacion_id")
    private @Nullable Consignacion consignacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public List<LineaVenta> getLineas() { return lineas; }
    public void setLineas(List<LineaVenta> lineas) { this.lineas = lineas; }

    public @Nullable Consignacion getConsignacion() { return consignacion; }
    public void setConsignacion(@Nullable Consignacion consignacion) { this.consignacion = consignacion; }
}
