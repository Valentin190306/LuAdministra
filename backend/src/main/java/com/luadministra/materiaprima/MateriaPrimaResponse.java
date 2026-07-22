package com.luadministra.materiaprima;

import org.eclipse.jdt.annotation.Nullable;

public record MateriaPrimaResponse(
        Long id,
        String nombre,
        String unidadMedida,
        Double stockActual,
        @Nullable Double stockMinimo,
        @Nullable Long categoriaId,
        @Nullable String categoriaNombre
) {
    public static MateriaPrimaResponse fromEntity(MateriaPrima mp) {
        var cat = mp.getCategoria();
        return new MateriaPrimaResponse(
                mp.getId(), mp.getNombre(), mp.getUnidadMedida(),
                mp.getStockActual(), mp.getStockMinimo(),
                cat != null ? cat.getId() : null,
                cat != null ? cat.getNombre() : null
        );
    }
}
