package com.luadministra.produccion;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProduccionRequest(
        @NotNull Long productoTerminadoId,
        @NotNull LocalDate fecha,
        @NotNull Double cantidadFabricada
) {}
