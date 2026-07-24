package com.luadministra.produccion;

import org.eclipse.jdt.annotation.Nullable;
import java.time.LocalDate;

public record ProduccionResponse(
        Long id,
        Long productoTerminadoId,
        String productoTerminadoNombre,
        LocalDate fecha,
        Double cantidadFabricada,
        @Nullable Integer diasVigencia,
        @Nullable LocalDate fechaVencimiento
) {
    public static ProduccionResponse fromEntity(Produccion p) {
        return new ProduccionResponse(
                p.getId(), p.getProductoTerminado().getId(),
                p.getProductoTerminado().getNombre(), p.getFecha(),
                p.getCantidadFabricada(),
                p.getDiasVigencia(),
                p.getDiasVigencia() != null ? p.getFecha().plusDays(p.getDiasVigencia()) : null
        );
    }
}
