package com.luadministra.lote;

import jakarta.validation.constraints.NotNull;
import org.eclipse.jdt.annotation.Nullable;
import java.time.LocalDate;

public record LoteRequest(
        @NotNull Long productoId,
        @NotNull LocalDate fecha,
        @NotNull Double cantidadFabricada,
        @Nullable Integer diasVigencia
) {}
