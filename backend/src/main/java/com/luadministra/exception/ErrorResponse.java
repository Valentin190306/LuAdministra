package com.luadministra.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String mensaje,
        String tipo,
        int codigo
) {}
