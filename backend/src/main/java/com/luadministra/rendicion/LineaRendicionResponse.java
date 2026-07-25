package com.luadministra.rendicion;

public record LineaRendicionResponse(
        Long id,
        Long lineaConsignacionId,
        Long productoId,
        String productoNombre,
        Double cantidadVendida,
        Double cantidadDevuelta
) {
    public static LineaRendicionResponse fromEntity(LineaRendicion lr) {
        return new LineaRendicionResponse(
                lr.getId(),
                lr.getLineaConsignacion().getId(),
                lr.getLineaConsignacion().getProducto().getId(),
                lr.getLineaConsignacion().getProducto().getNombre(),
                lr.getCantidadVendida(),
                lr.getCantidadDevuelta()
        );
    }
}
