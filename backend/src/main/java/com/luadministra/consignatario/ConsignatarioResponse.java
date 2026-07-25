package com.luadministra.consignatario;

import org.eclipse.jdt.annotation.Nullable;

public record ConsignatarioResponse(
        Long id,
        String nombre,
        @Nullable String contacto
) {
    public static ConsignatarioResponse fromEntity(Consignatario c) {
        return new ConsignatarioResponse(c.getId(), c.getNombre(), c.getContacto());
    }
}
