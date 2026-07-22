package com.luadministra.compra;

import jakarta.validation.constraints.NotNull;
import org.eclipse.jdt.annotation.Nullable;
import java.time.LocalDate;

public record CompraRequest(
        @NotNull Long materiaPrimaId,
        @NotNull LocalDate fecha,
        @NotNull Double cantidad,
        @NotNull Double precio,
        @Nullable String lugar,
        @Nullable String url,
        @Nullable String mlId
) {}
