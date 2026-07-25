package com.luadministra.lote;

import org.eclipse.jdt.annotation.Nullable;
import java.time.LocalDate;

public record LoteResponse(
        Long id,
        Long productoId,
        String productoNombre,
        LocalDate fecha,
        Double cantidadFabricada,
        @Nullable Integer diasVigencia,
        @Nullable LocalDate fechaVencimiento
) {
    public static LoteResponse fromEntity(Lote l) {
        return new LoteResponse(
                l.getId(), l.getProducto().getId(),
                l.getProducto().getNombre(), l.getFecha(),
                l.getCantidadFabricada(),
                l.getDiasVigencia(),
                l.getDiasVigencia() != null ? l.getFecha().plusDays(l.getDiasVigencia()) : null
        );
    }
}
