package com.luadministra.ml;

import java.time.LocalDateTime;

public record ConsultaPrecioMLResponse(
        Long id,
        Long compraId,
        String mlId,
        Double precio,
        LocalDateTime fechaHora
) {
    public static ConsultaPrecioMLResponse fromEntity(ConsultaPrecioML c) {
        return new ConsultaPrecioMLResponse(
                c.getId(),
                c.getCompra().getId(),
                c.getCompra().getMlId(),
                c.getPrecio(),
                c.getFechaHora()
        );
    }
}
