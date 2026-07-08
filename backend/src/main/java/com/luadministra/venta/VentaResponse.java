package com.luadministra.venta;

import java.time.LocalDate;

public record VentaResponse(
        Long id,
        Long productoTerminadoId,
        String productoTerminadoNombre,
        LocalDate fecha,
        Double cantidad
) {
    public static VentaResponse fromEntity(Venta v) {
        return new VentaResponse(v.getId(), v.getProductoTerminado().getId(),
                v.getProductoTerminado().getNombre(), v.getFecha(), v.getCantidad());
    }
}
