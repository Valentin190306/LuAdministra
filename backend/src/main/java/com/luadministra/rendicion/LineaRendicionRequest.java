package com.luadministra.rendicion;

import jakarta.validation.constraints.NotNull;

public record LineaRendicionRequest(
        @NotNull Long productoTerminadoId,
        @NotNull Double cantidadVendida,
        @NotNull Double cantidadDevuelta
) {}
