package com.luadministra.despacho;

public record StockConsignadoResponse(
        Long productoTerminadoId,
        String productoTerminadoNombre,
        Double cantidadDespachada,
        Double cantidadRendida,
        Double cantidadPendiente
) {}
