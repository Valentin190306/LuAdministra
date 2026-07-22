package com.luadministra.despacho;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record DespachoRequest(
        @NotNull Long colaboradoraId,
        @NotNull LocalDate fecha,
        @NotNull List<LineaDespachoRequest> productos
) {}
