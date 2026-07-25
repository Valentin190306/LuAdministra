package com.luadministra.rendicion;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record RendicionRequest(
        @NotNull Long consignacionId,
        @NotNull List<LineaRendicionRequest> productos,
        @NotNull Double montoEntregado,
        @NotNull LocalDate fecha
) {}
