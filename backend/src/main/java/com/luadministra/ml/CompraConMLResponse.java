package com.luadministra.ml;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CompraConMLResponse(
        Long compraId,
        Long materiaPrimaId,
        String materiaPrimaNombre,
        LocalDate fechaCompra,
        Double precioCompra,
        String lugar,
        String mlId,
        Double ultimoPrecioML,
        LocalDateTime ultimaConsultaML
) {}
