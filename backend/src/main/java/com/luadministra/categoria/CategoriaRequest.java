package com.luadministra.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.jdt.annotation.Nullable;

public record CategoriaRequest(
        @NotBlank String nombre,
        @Nullable Long categoriaPadreId,
        @NotNull TipoCategoria tipo
) {}
