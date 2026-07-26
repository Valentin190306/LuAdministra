package com.luadministra.lote;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.eclipse.jdt.annotation.Nullable;
import java.time.LocalDate;

public record LoteRequest(
        @NotNull Long productoId,
        @NotNull LocalDate fecha,
        @NotNull @Positive Double cantidadFabricada,
        @Nullable Integer diasVigencia
) {}
