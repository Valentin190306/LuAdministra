package com.luadministra.rendicion;

import jakarta.validation.constraints.NotNull;

public record LineaRendicionRequest(
        @NotNull Long lineaConsignacionId,
        @NotNull Double cantidadVendida,
        Double cantidadDevuelta
) {}
