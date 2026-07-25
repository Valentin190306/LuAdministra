package com.luadministra.venta;

public record LineaVentaResponse(
        Long id,
        Long productoId,
        String productoNombre,
        Double cantidad,
        Double precioUnitario
) {
    public static LineaVentaResponse fromEntity(LineaVenta l) {
        return new LineaVentaResponse(
                l.getId(),
                l.getProducto().getId(),
                l.getProducto().getNombre(),
                l.getCantidad(),
                l.getPrecioUnitario()
        );
    }
}
