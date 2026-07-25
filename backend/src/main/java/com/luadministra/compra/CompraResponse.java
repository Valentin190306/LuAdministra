package com.luadministra.compra;

import java.time.LocalDate;
import org.eclipse.jdt.annotation.Nullable;

public record CompraResponse(
        Long id,
        Long materiaPrimaId,
        String materiaPrimaNombre,
        LocalDate fecha,
        Double cantidad,
        Double precio,
        @Nullable String lugar,
        @Nullable String url,
        @Nullable Double precioMLReferencia
) {
     public static CompraResponse fromEntity(Compra c) {
        return new CompraResponse(c.getId(), c.getMateriaPrima().getId(),
                c.getMateriaPrima().getNombre(), c.getFecha(),
                c.getCantidad(), c.getPrecio(), c.getLugar(), c.getUrl(), c.getPrecioMLReferencia());
    }
}
