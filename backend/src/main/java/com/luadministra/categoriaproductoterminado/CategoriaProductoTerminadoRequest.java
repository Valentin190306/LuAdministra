package com.luadministra.categoriaproductoterminado;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.jdt.annotation.Nullable;

public record CategoriaProductoTerminadoRequest(
        @NotBlank String nombre,
        @Nullable Long categoriaPadreId
) {}
