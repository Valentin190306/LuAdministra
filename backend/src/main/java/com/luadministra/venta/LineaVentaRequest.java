package com.luadministra.venta;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.eclipse.jdt.annotation.Nullable;

public record LineaVentaRequest(
        @NotNull Long productoId,
        @NotNull @Positive Double cantidad,
        @Nullable @Positive Double precioUnitario
) {}
