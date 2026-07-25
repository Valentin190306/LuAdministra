package com.luadministra.producto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.jdt.annotation.Nullable;

public record ProductoRequest(
        @NotBlank String nombre,
        @NotNull Double precioVenta,
        @Nullable Double stockActual,
        @Nullable Double stockMinimo,
        @Nullable Long categoriaId
) {}
