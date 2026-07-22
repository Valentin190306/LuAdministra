package com.luadministra.productoterminado;

import org.eclipse.jdt.annotation.Nullable;

public record ProductoTerminadoResponse(
        Long id,
        String nombre,
        Double precioVenta,
        Double stockActual,
        Double stockDespachado,
        @Nullable Double stockMinimo,
        @Nullable Long categoriaId,
        @Nullable String categoriaNombre
) {
    public static ProductoTerminadoResponse fromEntity(ProductoTerminado pt) {
        return new ProductoTerminadoResponse(
                pt.getId(), pt.getNombre(), pt.getPrecioVenta(),
                pt.getStockActual(), 0.0, pt.getStockMinimo(),
                pt.getCategoria() != null ? pt.getCategoria().getId() : null,
                pt.getCategoria() != null ? pt.getCategoria().getNombre() : null
        );
    }

    public static ProductoTerminadoResponse fromEntity(ProductoTerminado pt, Double stockDespachado) {
        return new ProductoTerminadoResponse(
                pt.getId(), pt.getNombre(), pt.getPrecioVenta(),
                pt.getStockActual(), stockDespachado, pt.getStockMinimo(),
                pt.getCategoria() != null ? pt.getCategoria().getId() : null,
                pt.getCategoria() != null ? pt.getCategoria().getNombre() : null
        );
    }
}
