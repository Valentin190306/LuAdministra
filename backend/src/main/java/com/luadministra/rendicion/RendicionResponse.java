package com.luadministra.rendicion;

import java.time.LocalDate;
import java.util.List;

public record RendicionResponse(
        Long id,
        Long despachoId,
        Long colaboradoraId,
        String colaboradoraNombre,
        Double montoEntregado,
        LocalDate fecha,
        List<LineaRendicionResponse> productos
) {
    public static RendicionResponse fromEntity(Rendicion r) {
        return new RendicionResponse(
                r.getId(),
                r.getDespacho().getId(),
                r.getDespacho().getColaboradora().getId(),
                r.getDespacho().getColaboradora().getNombre(),
                r.getMontoEntregado(),
                r.getFecha(),
                r.getLineas().stream().map(LineaRendicionResponse::fromEntity).toList()
        );
    }
}
