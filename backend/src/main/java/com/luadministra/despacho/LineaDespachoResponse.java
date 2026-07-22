package com.luadministra.despacho;

public record LineaDespachoResponse(
        Long id,
        Long productoTerminadoId,
        String productoTerminadoNombre,
        Double cantidad,
        Double precioUnitario
) {
    public static LineaDespachoResponse fromEntity(LineaDespacho ld) {
        return new LineaDespachoResponse(
                ld.getId(),
                ld.getProductoTerminado().getId(),
                ld.getProductoTerminado().getNombre(),
                ld.getCantidad(),
                ld.getPrecioUnitario()
        );
    }
}
