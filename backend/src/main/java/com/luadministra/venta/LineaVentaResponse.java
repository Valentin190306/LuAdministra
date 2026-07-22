package com.luadministra.venta;

public record LineaVentaResponse(
        Long id,
        Long productoTerminadoId,
        String productoTerminadoNombre,
        Double cantidad,
        Double precioUnitario
) {
    public static LineaVentaResponse fromEntity(LineaVenta l) {
        return new LineaVentaResponse(
                l.getId(),
                l.getProductoTerminado().getId(),
                l.getProductoTerminado().getNombre(),
                l.getCantidad(),
                l.getPrecioUnitario()
        );
    }
}
