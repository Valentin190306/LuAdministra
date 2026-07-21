package com.luadministra.productoterminado;

public record ProductoTerminadoResponse(
        Long id,
        String nombre,
        Double precioVenta,
        Double stockActual,
        Double stockMinimo,
        Long categoriaId,
        String categoriaNombre
) {
    public static ProductoTerminadoResponse fromEntity(ProductoTerminado pt) {
        return new ProductoTerminadoResponse(
                pt.getId(), pt.getNombre(), pt.getPrecioVenta(),
                pt.getStockActual(), pt.getStockMinimo(),
                pt.getCategoria() != null ? pt.getCategoria().getId() : null,
                pt.getCategoria() != null ? pt.getCategoria().getNombre() : null
        );
    }
}
