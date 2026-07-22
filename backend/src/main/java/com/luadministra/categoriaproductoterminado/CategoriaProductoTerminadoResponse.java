package com.luadministra.categoriaproductoterminado;

public record CategoriaProductoTerminadoResponse(
        Long id,
        String nombre,
        Long categoriaPadreId,
        String categoriaPadreNombre
) {
    public static CategoriaProductoTerminadoResponse fromEntity(CategoriaProductoTerminado c) {
        var padre = c.getCategoriaPadre();
        return new CategoriaProductoTerminadoResponse(
                c.getId(), c.getNombre(),
                padre != null ? padre.getId() : null,
                padre != null ? padre.getNombre() : null
        );
    }
}
