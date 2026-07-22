package com.luadministra.venta;

import jakarta.validation.constraints.NotNull;

public record LineaVentaRequest(
        @NotNull Long productoTerminadoId,
        @NotNull Double cantidad
) {}
