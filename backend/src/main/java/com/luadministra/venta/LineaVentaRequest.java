package com.luadministra.venta;

import jakarta.validation.constraints.NotNull;
import org.eclipse.jdt.annotation.Nullable;

public record LineaVentaRequest(
        @NotNull Long productoId,
        @NotNull Double cantidad,
        @Nullable Double precioUnitario
) {}
