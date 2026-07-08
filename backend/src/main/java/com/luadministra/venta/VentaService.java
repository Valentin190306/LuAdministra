package com.luadministra.venta;

import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.StockInsuficienteException;
import com.luadministra.productoterminado.ProductoTerminado;
import com.luadministra.productoterminado.ProductoTerminadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoTerminadoRepository productoTerminadoRepository;

    public VentaService(VentaRepository ventaRepository, ProductoTerminadoRepository productoTerminadoRepository) {
        this.ventaRepository = ventaRepository;
        this.productoTerminadoRepository = productoTerminadoRepository;
    }

    public List<VentaResponse> listar() {
        return ventaRepository.findAll().stream()
                .map(VentaResponse::fromEntity)
                .toList();
    }

    public List<VentaResponse> listarPorPeriodo(LocalDate desde, LocalDate hasta) {
        return ventaRepository.findByFechaBetweenOrderByFechaDesc(desde, hasta).stream()
                .map(VentaResponse::fromEntity)
                .toList();
    }

    public VentaResponse obtener(Long id) {
        return VentaResponse.fromEntity(
                ventaRepository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada")));
    }

    @Transactional
    public VentaResponse crear(VentaRequest request) {
        ProductoTerminado pt = productoTerminadoRepository
                .findById(request.productoTerminadoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto terminado no encontrado"));

        if (pt.getStockActual() < request.cantidad()) {
            throw new StockInsuficienteException("Stock insuficiente de " + pt.getNombre());
        }

        pt.setStockActual(pt.getStockActual() - request.cantidad());
        productoTerminadoRepository.save(pt);

        Venta venta = new Venta();
        venta.setProductoTerminado(pt);
        venta.setFecha(request.fecha());
        venta.setCantidad(request.cantidad());

        return VentaResponse.fromEntity(ventaRepository.save(venta));
    }

    public void eliminar(Long id) {
        ventaRepository.deleteById(id);
    }
}
