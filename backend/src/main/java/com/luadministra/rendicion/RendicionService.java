package com.luadministra.rendicion;

import com.luadministra.despacho.Despacho;
import com.luadministra.despacho.DespachoRepository;
import com.luadministra.despacho.EstadoDespacho;
import com.luadministra.despacho.LineaDespacho;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.SolicitudInvalidaException;
import com.luadministra.productoterminado.ProductoTerminado;
import com.luadministra.productoterminado.ProductoTerminadoRepository;
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
    private final DespachoRepository despachoRepository;
    private final ProductoTerminadoRepository productoTerminadoRepository;
    private final VentaService ventaService;

    public RendicionService(RendicionRepository rendicionRepository,
                            DespachoRepository despachoRepository,
                            ProductoTerminadoRepository productoTerminadoRepository,
                            VentaService ventaService) {
        this.rendicionRepository = rendicionRepository;
        this.despachoRepository = despachoRepository;
        this.productoTerminadoRepository = productoTerminadoRepository;
        this.ventaService = ventaService;
    }

    public List<RendicionResponse> listarPorDespacho(Long despachoId) {
        return rendicionRepository.findByDespachoId(despachoId).stream()
                .map(RendicionResponse::fromEntity)
                .toList();
    }

    @Transactional
    public RendicionResponse crear(RendicionRequest request) {
        Despacho despacho = despachoRepository.findById(request.despachoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Despacho no encontrado"));

        if (despacho.getEstado() == EstadoDespacho.RENDIDO_TOTAL) {
            throw new SolicitudInvalidaException("El despacho ya está completamente rendido");
        }

        Map<Long, LineaDespacho> lineasDespacho = despacho.getLineas().stream()
                .collect(Collectors.toMap(ld -> ld.getProductoTerminado().getId(), ld -> ld));

        List<Rendicion> anteriores = rendicionRepository.findByDespachoId(request.despachoId());

        Map<Long, Double> rendidoAnteriorPorProducto = anteriores.stream()
                .flatMap(r -> r.getLineas().stream())
                .collect(Collectors.groupingBy(
                        lr -> lr.getProductoTerminado().getId(),
                        Collectors.summingDouble(lr -> lr.getCantidadVendida() + lr.getCantidadDevuelta())
                ));

        List<LineaVentaRequest> lineasVenta = new ArrayList<>();

        for (LineaRendicionRequest prodReq : request.productos()) {
            LineaDespacho lineaDespacho = lineasDespacho.get(prodReq.productoTerminadoId());
            if (lineaDespacho == null) {
                throw new SolicitudInvalidaException(
                        "El producto ID " + prodReq.productoTerminadoId() + " no está en el despacho");
            }
            double disponible = lineaDespacho.getCantidad()
                    - rendidoAnteriorPorProducto.getOrDefault(prodReq.productoTerminadoId(), 0.0);
            if (prodReq.cantidadVendida() + prodReq.cantidadDevuelta() > disponible) {
                throw new SolicitudInvalidaException(
                        "La suma vendido+devuelto del producto " + lineaDespacho.getProductoTerminado().getNombre()
                        + " (" + (prodReq.cantidadVendida() + prodReq.cantidadDevuelta())
                        + ") supera el disponible (" + disponible + ")");
            }

            if (prodReq.cantidadVendida() > 0) {
                lineasVenta.add(new LineaVentaRequest(
                        prodReq.productoTerminadoId(),
                        prodReq.cantidadVendida(),
                        lineaDespacho.getPrecioUnitario()));
            }

            if (prodReq.cantidadDevuelta() > 0) {
                ProductoTerminado pt = productoTerminadoRepository.findById(prodReq.productoTerminadoId())
                        .orElseThrow(() -> new RecursoNoEncontradoException("Producto terminado no encontrado"));
                pt.setStockActual(pt.getStockActual() + prodReq.cantidadDevuelta());
                productoTerminadoRepository.save(pt);
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
                .mapToDouble(p -> p.cantidadVendida() + p.cantidadDevuelta())
                .sum();
        double totalDespachado = despacho.getLineas().stream()
                .mapToDouble(LineaDespacho::getCantidad)
                .sum();
        double totalRendido = totalRendidoAnterior + totalRendidoAhora;
        if (totalRendido >= totalDespachado) {
            despacho.setEstado(EstadoDespacho.RENDIDO_TOTAL);
        } else {
            despacho.setEstado(EstadoDespacho.RENDIDO_PARCIAL);
        }
        despachoRepository.save(despacho);

        Rendicion rendicion = new Rendicion();
        rendicion.setDespacho(despacho);
        rendicion.setMontoEntregado(request.montoEntregado());
        rendicion.setFecha(request.fecha());

        for (LineaRendicionRequest prodReq : request.productos()) {
            LineaRendicion lr = new LineaRendicion();
            lr.setRendicion(rendicion);
            lr.setProductoTerminado(productoTerminadoRepository.getReferenceById(prodReq.productoTerminadoId()));
            lr.setCantidadVendida(prodReq.cantidadVendida());
            lr.setCantidadDevuelta(prodReq.cantidadDevuelta());
            rendicion.getLineas().add(lr);
        }

        return RendicionResponse.fromEntity(rendicionRepository.save(rendicion));
    }
}
