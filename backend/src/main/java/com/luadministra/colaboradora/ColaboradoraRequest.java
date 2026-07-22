package com.luadministra.colaboradora;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.jdt.annotation.Nullable;

public record ColaboradoraRequest(
        @NotBlank String nombre,
        @Nullable String contacto
) {}
