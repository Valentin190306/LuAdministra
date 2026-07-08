package com.luadministra.venta;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record VentaRequest(
        @NotNull Long productoTerminadoId,
        @NotNull LocalDate fecha,
        @NotNull Double cantidad
) {}
