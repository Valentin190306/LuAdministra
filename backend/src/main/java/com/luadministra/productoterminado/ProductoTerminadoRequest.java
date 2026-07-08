package com.luadministra.productoterminado;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.jdt.annotation.Nullable;

public record ProductoTerminadoRequest(
        @NotBlank String nombre,
        @NotNull Double precioVenta,
        @Nullable Double stockMinimo
) {}
