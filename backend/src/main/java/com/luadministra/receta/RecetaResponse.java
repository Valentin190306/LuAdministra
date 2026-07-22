package com.luadministra.receta;

import java.util.List;
import org.eclipse.jdt.annotation.Nullable;

public record RecetaResponse(
        Long id,
        Long productoTerminadoId,
        String productoTerminadoNombre,
        List<RecetaDetalleResponse> detalles,
        @Nullable String notas
) {
    public static RecetaResponse fromEntity(Receta r) {
        return new RecetaResponse(r.getId(), r.getProductoTerminado().getId(),
                r.getProductoTerminado().getNombre(),
                r.getDetalles().stream().map(RecetaDetalleResponse::fromEntity).toList(),
                r.getNotas());
    }
}
