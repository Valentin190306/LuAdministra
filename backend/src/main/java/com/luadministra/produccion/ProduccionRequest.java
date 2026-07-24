package com.luadministra.produccion;

import jakarta.validation.constraints.NotNull;
import org.eclipse.jdt.annotation.Nullable;
import java.time.LocalDate;

public record ProduccionRequest(
        @NotNull Long productoTerminadoId,
        @NotNull LocalDate fecha,
        @NotNull Double cantidadFabricada,
        @Nullable Integer diasVigencia
) {}
