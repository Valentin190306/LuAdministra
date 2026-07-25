package com.luadministra.consignacion;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record ConsignacionRequest(
        @NotNull Long consignatarioId,
        @NotNull LocalDate fecha,
        @NotNull List<LineaConsignacionRequest> productos
) {}
