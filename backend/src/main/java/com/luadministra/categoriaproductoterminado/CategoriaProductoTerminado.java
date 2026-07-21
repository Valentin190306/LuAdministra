package com.luadministra.categoriaproductoterminado;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.jdt.annotation.Nullable;

@Entity
public class CategoriaProductoTerminado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    private @Nullable CategoriaProductoTerminado categoriaPadre;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public @Nullable CategoriaProductoTerminado getCategoriaPadre() { return categoriaPadre; }
    public void setCategoriaPadre(@Nullable CategoriaProductoTerminado categoriaPadre) { this.categoriaPadre = categoriaPadre; }
}
