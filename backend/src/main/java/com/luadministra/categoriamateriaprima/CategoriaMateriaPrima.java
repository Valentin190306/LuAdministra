package com.luadministra.categoriamateriaprima;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.jdt.annotation.Nullable;

@Entity
public class CategoriaMateriaPrima {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    private @Nullable CategoriaMateriaPrima categoriaPadre;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public @Nullable CategoriaMateriaPrima getCategoriaPadre() { return categoriaPadre; }
    public void setCategoriaPadre(@Nullable CategoriaMateriaPrima categoriaPadre) { this.categoriaPadre = categoriaPadre; }
}
