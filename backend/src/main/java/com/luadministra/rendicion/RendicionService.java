package com.luadministra.rendicion;

import com.luadministra.consignacion.Consignacion;
import com.luadministra.consignacion.ConsignacionRepository;
import com.luadministra.consignacion.EstadoConsignacion;
import com.luadministra.consignacion.LineaConsignacion;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.SolicitudInvalidaException;
import com.luadministra.producto.Producto;
import com.luadministra.producto.ProductoRepository;
import com.luadministra.venta.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RendicionService {

    private final RendicionRepository rendicionRepository;
    private final ConsignacionRepository consignacionRepository;
    private final ProductoRepository productoRepository;
    private final VentaService ventaService;

    public RendicionService(RendicionRepository rendicionRepository,
                            ConsignacionRepository consignacionRepository,
                            ProductoRepository productoRepository,
                            VentaService ventaService) {
        this.rendicionRepository = rendicionRepository;
        this.consignacionRepository = consignacionRepository;
        this.productoRepository = productoRepository;
        this.ventaService = ventaService;
    }

    public List<RendicionResponse> listarPorConsignacion(Long consignacionId) {
        return rendicionRepository.findByConsignacionId(consignacionId).stream()
                .map(RendicionResponse::fromEntity)
                .toList();
    }

    @Transactional
    public RendicionResponse crear(RendicionRequest request) {
        Consignacion consignacion = consignacionRepository.findById(request.consignacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Consignacion no encontrada"));

        if (consignacion.getEstado() == EstadoConsignacion.RENDIDO_TOTAL) {
            throw new SolicitudInvalidaException("La consignacion ya está completamente rendida");
        }

        Map<Long, LineaConsignacion> lineasConsignacion = consignacion.getLineas().stream()
                .collect(Collectors.toMap(lc -> lc.getId(), lc -> lc));

        List<Rendicion> anteriores = rendicionRepository.findByConsignacionId(request.consignacionId());

        Map<Long, Double> rendidoAnteriorPorLinea = anteriores.stream()
                .flatMap(r -> r.getLineas().stream())
                .collect(Collectors.groupingBy(
                        lr -> lr.getLineaConsignacion().getId(),
                        Collectors.summingDouble(lr -> lr.getCantidadVendida() + lr.getCantidadDevuelta())
                ));

        List<LineaVentaRequest> lineasVenta = new ArrayList<>();

        for (LineaRendicionRequest prodReq : request.productos()) {
            LineaConsignacion lineaConsignacion = lineasConsignacion.get(prodReq.lineaConsignacionId());
            if (lineaConsignacion == null) {
                throw new SolicitudInvalidaException(
                        "La línea de consignación ID " + prodReq.lineaConsignacionId() + " no está en la consignacion");
            }
            double devuelta = devueltaOrDefault(prodReq);
            double disponible = lineaConsignacion.getCantidad()
                    - rendidoAnteriorPorLinea.getOrDefault(prodReq.lineaConsignacionId(), 0.0);
            if (prodReq.cantidadVendida() + devuelta > disponible) {
                throw new SolicitudInvalidaException(
                        "La suma vendido+devuelto del producto " + lineaConsignacion.getProducto().getNombre()
                        + " (" + (prodReq.cantidadVendida() + devuelta)
                        + ") supera el disponible (" + disponible + ")");
            }

            if (prodReq.cantidadVendida() > 0) {
                lineasVenta.add(new LineaVentaRequest(
                        lineaConsignacion.getProducto().getId(),
                        prodReq.cantidadVendida(),
                        lineaConsignacion.getPrecioUnitario()));
            }

            if (devuelta > 0) {
                Producto producto = lineaConsignacion.getProducto();
                producto.setStockActual(producto.getStockActual() + devuelta);
                productoRepository.save(producto);
            }
        }

        if (!lineasVenta.isEmpty()) {
            VentaRequest ventaRequest = new VentaRequest(request.fecha(), lineasVenta);
            ventaService.crear(ventaRequest);
        }

        double totalRendidoAnterior = anteriores.stream()
                .flatMap(r -> r.getLineas().stream())
                .mapToDouble(lr -> lr.getCantidadVendida() + lr.getCantidadDevuelta())
                .sum();
        double totalRendidoAhora = request.productos().stream()
                .mapToDouble(p -> p.cantidadVendida() + devueltaOrDefault(p))
                .sum();
        double totalConsignado = consignacion.getLineas().stream()
                .mapToDouble(lc -> lc.getCantidad())
                .sum();
        double totalRendido = totalRendidoAnterior + totalRendidoAhora;
        if (totalRendido >= totalConsignado) {
            consignacion.setEstado(EstadoConsignacion.RENDIDO_TOTAL);
        } else {
            consignacion.setEstado(EstadoConsignacion.RENDIDO_PARCIAL);
        }
        consignacionRepository.save(consignacion);

        Rendicion rendicion = new Rendicion();
        rendicion.setConsignacion(consignacion);
        rendicion.setMontoEntregado(request.montoEntregado());
        rendicion.setFecha(request.fecha());

        for (LineaRendicionRequest prodReq : request.productos()) {
            LineaRendicion lr = new LineaRendicion();
            lr.setRendicion(rendicion);
            lr.setLineaConsignacion(lineasConsignacion.get(prodReq.lineaConsignacionId()));
            lr.setCantidadVendida(prodReq.cantidadVendida());
            lr.setCantidadDevuelta(devueltaOrDefault(prodReq));
            rendicion.getLineas().add(lr);
        }

        return RendicionResponse.fromEntity(rendicionRepository.save(rendicion));
    }

    private static double devueltaOrDefault(LineaRendicionRequest r) {
        return r.cantidadDevuelta() != null ? r.cantidadDevuelta() : 0.0;
    }
}
