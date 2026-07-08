package com.luadministra.receta;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RecetaRequest(
        @NotNull Long productoTerminadoId,
        @NotNull List<RecetaDetalleRequest> detalles
) {}
