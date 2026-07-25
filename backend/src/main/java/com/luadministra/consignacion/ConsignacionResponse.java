package com.luadministra.consignacion;

import java.time.LocalDate;
import java.util.List;

public record ConsignacionResponse(
        Long id,
        Long consignatarioId,
        String consignatarioNombre,
        LocalDate fecha,
        EstadoConsignacion estado,
        List<LineaConsignacionResponse> productos
) {
    public static ConsignacionResponse fromEntity(Consignacion c) {
        return new ConsignacionResponse(
                c.getId(),
                c.getConsignatario().getId(),
                c.getConsignatario().getNombre(),
                c.getFecha(),
                c.getEstado(),
                c.getLineas().stream().map(LineaConsignacionResponse::fromEntity).toList()
        );
    }
}
