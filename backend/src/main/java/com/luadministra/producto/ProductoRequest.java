package com.luadministra.producto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.eclipse.jdt.annotation.Nullable;

public record ProductoRequest(
        @NotBlank String nombre,
        @NotNull @Positive Double precioVenta,
        @Nullable @PositiveOrZero Double stockActual,
        @Nullable @PositiveOrZero Double stockMinimo,
        @Nullable Long categoriaId
) {}
