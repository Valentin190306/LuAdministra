package com.luadministra.categoriamateriaprima;

public record CategoriaMateriaPrimaResponse(
        Long id,
        String nombre,
        Long categoriaPadreId,
        String categoriaPadreNombre
) {
    public static CategoriaMateriaPrimaResponse fromEntity(CategoriaMateriaPrima c) {
        return new CategoriaMateriaPrimaResponse(
                c.getId(), c.getNombre(),
                c.getCategoriaPadre() != null ? c.getCategoriaPadre().getId() : null,
                c.getCategoriaPadre() != null ? c.getCategoriaPadre().getNombre() : null
        );
    }
}
