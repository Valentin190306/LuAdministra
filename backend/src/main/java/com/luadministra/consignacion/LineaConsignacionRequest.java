package com.luadministra.consignacion;

import jakarta.validation.constraints.NotNull;

public record LineaConsignacionRequest(
        @NotNull Long productoId,
        @NotNull Double cantidad
) {}
