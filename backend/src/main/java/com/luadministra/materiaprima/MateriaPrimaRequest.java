package com.luadministra.materiaprima;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.eclipse.jdt.annotation.Nullable;

public record MateriaPrimaRequest(
        @NotBlank String nombre,
        @NotBlank String unidadMedida,
        @Nullable @PositiveOrZero Double stockActual,
        @Nullable @PositiveOrZero Double stockMinimo,
        @Nullable Long categoriaId
) {}
