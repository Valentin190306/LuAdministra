package com.luadministra.materiaprima;

public record MateriaPrimaResponse(
        Long id,
        String nombre,
        String unidadMedida,
        Double stockActual,
        Double stockMinimo,
        Long categoriaId,
        String categoriaNombre
) {
    public static MateriaPrimaResponse fromEntity(MateriaPrima mp) {
        return new MateriaPrimaResponse(
                mp.getId(), mp.getNombre(), mp.getUnidadMedida(),
                mp.getStockActual(), mp.getStockMinimo(),
                mp.getCategoria() != null ? mp.getCategoria().getId() : null,
                mp.getCategoria() != null ? mp.getCategoria().getNombre() : null
        );
    }
}
