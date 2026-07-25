package com.luadministra.consignatario;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.jdt.annotation.Nullable;

public record ConsignatarioRequest(
        @NotBlank String nombre,
        @Nullable String contacto
) {}
