package com.luadministra.despacho;

import java.time.LocalDate;
import java.util.List;

public record DespachoResponse(
        Long id,
        Long colaboradoraId,
        String colaboradoraNombre,
        LocalDate fecha,
        EstadoDespacho estado,
        List<LineaDespachoResponse> productos
) {
    public static DespachoResponse fromEntity(Despacho d) {
        return new DespachoResponse(
                d.getId(),
                d.getColaboradora().getId(),
                d.getColaboradora().getNombre(),
                d.getFecha(),
                d.getEstado(),
                d.getLineas().stream().map(LineaDespachoResponse::fromEntity).toList()
        );
    }
}
