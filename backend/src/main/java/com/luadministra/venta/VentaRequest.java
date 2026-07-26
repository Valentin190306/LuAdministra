package com.luadministra.venta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record VentaRequest(
        @NotNull LocalDate fecha,
        @NotEmpty @NotNull List<@Valid LineaVentaRequest> lineas
) {}
