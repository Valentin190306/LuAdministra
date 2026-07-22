package com.luadministra.venta;

import java.time.LocalDate;
import java.util.List;

public record VentaResponse(
        Long id,
        LocalDate fecha,
        List<LineaVentaResponse> lineas,
        Double total
) {
    public static VentaResponse fromEntity(Venta v) {
        List<LineaVentaResponse> lineasResp = v.getLineas().stream()
                .map(LineaVentaResponse::fromEntity)
                .toList();
        double total = lineasResp.stream()
                .mapToDouble(l -> l.cantidad() * l.precioUnitario())
                .sum();
        return new VentaResponse(v.getId(), v.getFecha(), lineasResp, total);
    }
}
