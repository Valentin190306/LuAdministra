package com.luadministra.rendicion;

public record LineaRendicionResponse(
        Long id,
        Long productoTerminadoId,
        String productoTerminadoNombre,
        Double cantidadVendida,
        Double cantidadDevuelta
) {
    public static LineaRendicionResponse fromEntity(LineaRendicion lr) {
        return new LineaRendicionResponse(
                lr.getId(),
                lr.getProductoTerminado().getId(),
                lr.getProductoTerminado().getNombre(),
                lr.getCantidadVendida(),
                lr.getCantidadDevuelta()
        );
    }
}
