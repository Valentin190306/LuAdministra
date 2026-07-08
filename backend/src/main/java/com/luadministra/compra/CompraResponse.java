package com.luadministra.compra;

import java.time.LocalDate;

public record CompraResponse(
        Long id,
        Long materiaPrimaId,
        String materiaPrimaNombre,
        LocalDate fecha,
        Double cantidad,
        Double precio,
        String lugar
) {
     public static CompraResponse fromEntity(Compra c) {
        return new CompraResponse(c.getId(), c.getMateriaPrima().getId(),
                c.getMateriaPrima().getNombre(), c.getFecha(),
                c.getCantidad(), c.getPrecio(), c.getLugar());
    }
}
