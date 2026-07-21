package com.luadministra.categoriaproductoterminado;

public record CategoriaProductoTerminadoResponse(
        Long id,
        String nombre,
        Long categoriaPadreId,
        String categoriaPadreNombre
) {
    public static CategoriaProductoTerminadoResponse fromEntity(CategoriaProductoTerminado c) {
        return new CategoriaProductoTerminadoResponse(
                c.getId(), c.getNombre(),
                c.getCategoriaPadre() != null ? c.getCategoriaPadre().getId() : null,
                c.getCategoriaPadre() != null ? c.getCategoriaPadre().getNombre() : null
        );
    }
}
