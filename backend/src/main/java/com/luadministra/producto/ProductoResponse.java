package com.luadministra.producto;

import org.eclipse.jdt.annotation.Nullable;

public record ProductoResponse(
        Long id,
        String nombre,
        Double precioVenta,
        Double stockActual,
        Double stockDespachado,
        @Nullable Double stockMinimo,
        @Nullable Long categoriaId,
        @Nullable String categoriaNombre
) {
    public static ProductoResponse fromEntity(Producto p) {
        var cat = p.getCategoria();
        return new ProductoResponse(
                p.getId(), p.getNombre(), p.getPrecioVenta(),
                p.getStockActual(), 0.0, p.getStockMinimo(),
                cat != null ? cat.getId() : null,
                cat != null ? cat.getNombre() : null
        );
    }

    public static ProductoResponse fromEntity(Producto p, Double stockDespachado) {
        var cat = p.getCategoria();
        return new ProductoResponse(
                p.getId(), p.getNombre(), p.getPrecioVenta(),
                p.getStockActual(), stockDespachado, p.getStockMinimo(),
                cat != null ? cat.getId() : null,
                cat != null ? cat.getNombre() : null
        );
    }
}
