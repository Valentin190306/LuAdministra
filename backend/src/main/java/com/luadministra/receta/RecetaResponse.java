package com.luadministra.receta;

import java.util.List;
import org.eclipse.jdt.annotation.Nullable;

public record RecetaResponse(
        Long id,
        Long productoId,
        String productoNombre,
        List<RecetaDetalleResponse> detalles,
        @Nullable String notas
) {
    public static RecetaResponse fromEntity(Receta r) {
        return new RecetaResponse(r.getId(), r.getProducto().getId(),
                r.getProducto().getNombre(),
                r.getDetalles().stream().map(RecetaDetalleResponse::fromEntity).toList(),
                r.getNotas());
    }
}
