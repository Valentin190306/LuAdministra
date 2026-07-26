package com.luadministra.compra;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.eclipse.jdt.annotation.Nullable;
import java.time.LocalDate;

public record CompraRequest(
        @NotNull Long materiaPrimaId,
        @NotNull LocalDate fecha,
        @NotNull @Positive Double cantidad,
        @NotNull @Positive Double precio,
        @Nullable String lugar,
        @Nullable String url,
        @Nullable Double precioMLReferencia
) {}
