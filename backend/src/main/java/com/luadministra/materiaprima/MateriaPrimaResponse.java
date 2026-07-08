package com.luadministra.materiaprima;

public record MateriaPrimaResponse(
        Long id,
        String nombre,
        String unidadMedida,
        Double stockActual,
        Double stockMinimo
) {
    public static MateriaPrimaResponse fromEntity(MateriaPrima mp) {
        return new MateriaPrimaResponse(mp.getId(), mp.getNombre(),
                mp.getUnidadMedida(), mp.getStockActual(), mp.getStockMinimo());
    }
}
