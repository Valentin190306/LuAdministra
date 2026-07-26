package com.luadministra.rendicion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.eclipse.jdt.annotation.Nullable;

public record LineaRendicionRequest(
        @NotNull Long lineaConsignacionId,
        @NotNull @PositiveOrZero Double cantidadVendida,
        @Nullable @PositiveOrZero Double cantidadDevuelta
) {}
