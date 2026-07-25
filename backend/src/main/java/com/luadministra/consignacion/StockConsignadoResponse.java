package com.luadministra.consignacion;

public record StockConsignadoResponse(
        Long productoId,
        String productoNombre,
        Double cantidadDespachada,
        Double cantidadRendida,
        Double cantidadPendiente
) {}
