package com.luadministra.venta;

import com.luadministra.dto.PaginatedResponse;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.StockInsuficienteException;
import com.luadministra.productoterminado.ProductoTerminado;
import com.luadministra.productoterminado.ProductoTerminadoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoTerminadoRepository productoTerminadoRepository;

    public VentaService(VentaRepository ventaRepository, ProductoTerminadoRepository productoTerminadoRepository) {
        this.ventaRepository = ventaRepository;
        this.productoTerminadoRepository = productoTerminadoRepository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<VentaResponse> listar(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(sortDir != null && sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "fecha");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Venta> ventaPage = ventaRepository.findAll(pageable);
        List<VentaResponse> content = ventaPage.stream().map(VentaResponse::fromEntity).toList();
        return PaginatedResponse.from(ventaPage, content);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<VentaResponse> listarPorPeriodo(LocalDate desde, LocalDate hasta, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"));
        Page<Venta> ventaPage = ventaRepository.findByFechaBetween(desde, hasta, pageable);
        List<VentaResponse> content = ventaPage.stream().map(VentaResponse::fromEntity).toList();
        return PaginatedResponse.from(ventaPage, content);
    }

    @Transactional(readOnly = true)
    public VentaResponse obtener(Long id) {
        return VentaResponse.fromEntity(
                ventaRepository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada")));
    }

    @Transactional
    public VentaResponse crear(VentaRequest request) {
        Venta venta = new Venta();
        venta.setFecha(request.fecha());

        List<LineaVenta> lineas = new ArrayList<>();
        for (LineaVentaRequest lineaReq : request.lineas()) {
            ProductoTerminado pt = productoTerminadoRepository
                    .findById(lineaReq.productoTerminadoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto terminado no encontrado"));

            if (pt.getStockActual() < lineaReq.cantidad()) {
                throw new StockInsuficienteException("Stock insuficiente de " + pt.getNombre());
            }

            pt.setStockActual(pt.getStockActual() - lineaReq.cantidad());
            productoTerminadoRepository.save(pt);

            LineaVenta linea = new LineaVenta();
            linea.setVenta(venta);
            linea.setProductoTerminado(pt);
            linea.setCantidad(lineaReq.cantidad());
            linea.setPrecioUnitario(pt.getPrecioVenta());
            lineas.add(linea);
        }

        venta.setLineas(lineas);
        Venta saved = ventaRepository.save(venta);
        return VentaResponse.fromEntity(saved);
    }

    @Transactional
    public void eliminar(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada"));

        for (LineaVenta linea : venta.getLineas()) {
            ProductoTerminado pt = linea.getProductoTerminado();
            pt.setStockActual(pt.getStockActual() + linea.getCantidad());
            productoTerminadoRepository.save(pt);
        }

        ventaRepository.delete(venta);
    }
}
