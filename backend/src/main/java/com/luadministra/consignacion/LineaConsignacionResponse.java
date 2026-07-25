package com.luadministra.consignacion;

public record LineaConsignacionResponse(
        Long id,
        Long productoId,
        String productoNombre,
        Double cantidad,
        Double precioUnitario
) {
    public static LineaConsignacionResponse fromEntity(LineaConsignacion lc) {
        return new LineaConsignacionResponse(
                lc.getId(),
                lc.getProducto().getId(),
                lc.getProducto().getNombre(),
                lc.getCantidad(),
                lc.getPrecioUnitario()
        );
    }
}
