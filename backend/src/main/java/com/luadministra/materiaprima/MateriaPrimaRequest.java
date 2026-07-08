package com.luadministra.materiaprima;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.jdt.annotation.Nullable;

public record MateriaPrimaRequest(
        @NotBlank String nombre,
        @NotBlank String unidadMedida,
        @Nullable Double stockMinimo
) {}
