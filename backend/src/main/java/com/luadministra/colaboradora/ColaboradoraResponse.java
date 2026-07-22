package com.luadministra.colaboradora;

import org.eclipse.jdt.annotation.Nullable;

public record ColaboradoraResponse(
        Long id,
        String nombre,
        @Nullable String contacto
) {
    public static ColaboradoraResponse fromEntity(Colaboradora c) {
        return new ColaboradoraResponse(c.getId(), c.getNombre(), c.getContacto());
    }
}
