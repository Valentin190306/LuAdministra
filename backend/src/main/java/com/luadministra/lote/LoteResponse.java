package com.luadministra.lote;

import org.eclipse.jdt.annotation.Nullable;
import java.time.LocalDate;

public record LoteResponse(
        Long id,
        Long productoId,
        String productoNombre,
        LocalDate fecha,
        Double cantidadFabricada,
        @Nullable Integer diasVigencia,
        @Nullable LocalDate fechaVencimiento
) {
    public static LoteResponse fromEntity(Lote l) {
        var prod = l.getProducto();
        Long prodId = prod != null ? prod.getId() : null;
        String prodNombre = prod != null ? prod.getNombre() : null;
        LocalDate fecha = l.getFecha();
        Integer dv = l.getDiasVigencia();
        return new LoteResponse(
                l.getId(), prodId, prodNombre, fecha,
                l.getCantidadFabricada(),
                dv,
                fecha != null && dv != null ? fecha.plusDays(dv) : null
        );
    }
}
