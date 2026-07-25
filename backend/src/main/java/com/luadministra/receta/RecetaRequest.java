package com.luadministra.receta;

import jakarta.validation.constraints.NotNull;
import org.eclipse.jdt.annotation.Nullable;
import java.util.List;

public record RecetaRequest(
        @NotNull Long productoId,
        @NotNull List<RecetaDetalleRequest> detalles,
        @Nullable String notas
) {}
