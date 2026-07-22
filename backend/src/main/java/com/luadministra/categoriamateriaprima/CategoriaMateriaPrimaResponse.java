package com.luadministra.categoriamateriaprima;

public record CategoriaMateriaPrimaResponse(
        Long id,
        String nombre,
        Long categoriaPadreId,
        String categoriaPadreNombre
) {
    public static CategoriaMateriaPrimaResponse fromEntity(CategoriaMateriaPrima c) {
        var padre = c.getCategoriaPadre();
        return new CategoriaMateriaPrimaResponse(
                c.getId(), c.getNombre(),
                padre != null ? padre.getId() : null,
                padre != null ? padre.getNombre() : null
        );
    }
}
