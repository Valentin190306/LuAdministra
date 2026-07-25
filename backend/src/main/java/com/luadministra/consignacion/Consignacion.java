package com.luadministra.consignacion;

import com.luadministra.consignatario.Consignatario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "consignacion")
public class Consignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consignatario_id", nullable = false)
    private Consignatario consignatario;

    @NotNull
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoConsignacion estado = EstadoConsignacion.PENDIENTE;

    @OneToMany(mappedBy = "consignacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaConsignacion> lineas = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Consignatario getConsignatario() { return consignatario; }
    public void setConsignatario(Consignatario consignatario) { this.consignatario = consignatario; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public EstadoConsignacion getEstado() { return estado; }
    public void setEstado(EstadoConsignacion estado) { this.estado = estado; }

    public List<LineaConsignacion> getLineas() { return lineas; }
    public void setLineas(List<LineaConsignacion> lineas) { this.lineas = lineas; }
}
