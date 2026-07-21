package com.luadministra.categoriamateriaprima;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.jdt.annotation.Nullable;

public record CategoriaMateriaPrimaRequest(
        @NotBlank String nombre,
        @Nullable Long categoriaPadreId
) {}
