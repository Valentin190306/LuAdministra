package com.luadministra.categoria;

import org.eclipse.jdt.annotation.Nullable;

public record CategoriaResponse(
        Long id,
        String nombre,
        @Nullable Long categoriaPadreId,
        @Nullable String categoriaPadreNombre,
        TipoCategoria tipo
) {
    public static CategoriaResponse fromEntity(Categoria c) {
        var padre = c.getCategoriaPadre();
        return new CategoriaResponse(
                c.getId(), c.getNombre(),
                padre != null ? padre.getId() : null,
                padre != null ? padre.getNombre() : null,
                c.getTipo()
        );
    }
}
