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
        var cat = pt.getCategoria();
        return new ProductoTerminadoResponse(
                pt.getId(), pt.getNombre(), pt.getPrecioVenta(),
                pt.getStockActual(), 0.0, pt.getStockMinimo(),
                cat != null ? cat.getId() : null,
                cat != null ? cat.getNombre() : null
        );
    }

    public static ProductoTerminadoResponse fromEntity(ProductoTerminado pt, Double stockDespachado) {
        var cat = pt.getCategoria();
        return new ProductoTerminadoResponse(
                pt.getId(), pt.getNombre(), pt.getPrecioVenta(),
                pt.getStockActual(), stockDespachado, pt.getStockMinimo(),
                cat != null ? cat.getId() : null,
                cat != null ? cat.getNombre() : null
        );
    }
}
