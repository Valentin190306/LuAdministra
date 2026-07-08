package com.luadministra.receta;

public record RecetaDetalleResponse(
        Long id,
        Long materiaPrimaId,
        String materiaPrimaNombre,
        Double cantidad
) {
    public static RecetaDetalleResponse fromEntity(RecetaDetalle d) {
        return new RecetaDetalleResponse(d.getId(), d.getMateriaPrima().getId(),
                d.getMateriaPrima().getNombre(), d.getCantidad());
    }
}
