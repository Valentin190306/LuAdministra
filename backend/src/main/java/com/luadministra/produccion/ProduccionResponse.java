package com.luadministra.produccion;

import java.time.LocalDate;

public record ProduccionResponse(
        Long id,
        Long productoTerminadoId,
        String productoTerminadoNombre,
        LocalDate fecha,
        Double cantidadFabricada
) {
    public static ProduccionResponse fromEntity(Produccion p) {
        return new ProduccionResponse(p.getId(), p.getProductoTerminado().getId(),
                p.getProductoTerminado().getNombre(), p.getFecha(), p.getCantidadFabricada());
    }
}
