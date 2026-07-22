package com.luadministra.venta;

import jakarta.validation.constraints.NotNull;
import org.eclipse.jdt.annotation.Nullable;

public record LineaVentaRequest(
        @NotNull Long productoTerminadoId,
        @NotNull Double cantidad,
        @Nullable Double precioUnitario
) {}
