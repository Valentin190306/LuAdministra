package com.luadministra.despacho;

import jakarta.validation.constraints.NotNull;

public record LineaDespachoRequest(
        @NotNull Long productoTerminadoId,
        @NotNull Double cantidad
) {}
