package com.luadministra.rendicion;

import java.time.LocalDate;
import java.util.List;

public record RendicionResponse(
        Long id,
        Long consignacionId,
        Long consignatarioId,
        String consignatarioNombre,
        Double montoEntregado,
        LocalDate fecha,
        List<LineaRendicionResponse> productos
) {
    public static RendicionResponse fromEntity(Rendicion r) {
        return new RendicionResponse(
                r.getId(),
                r.getConsignacion().getId(),
                r.getConsignacion().getConsignatario().getId(),
                r.getConsignacion().getConsignatario().getNombre(),
                r.getMontoEntregado(),
                r.getFecha(),
                r.getLineas().stream().map(LineaRendicionResponse::fromEntity).toList()
        );
    }
}
