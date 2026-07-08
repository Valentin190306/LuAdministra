package com.luadministra.receta;

import jakarta.validation.constraints.NotNull;

public record RecetaDetalleRequest(
        @NotNull Long materiaPrimaId,
        @NotNull Double cantidad
) {}
