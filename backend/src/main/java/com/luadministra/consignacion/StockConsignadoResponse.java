package com.luadministra.consignacion;

public record StockConsignadoResponse(
        Long productoId,
        String productoNombre,
        Double cantidadConsignada,
        Double cantidadRendida,
        Double cantidadPendiente
) {}
