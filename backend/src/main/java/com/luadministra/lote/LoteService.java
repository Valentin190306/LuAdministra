package com.luadministra.lote;

import com.luadministra.dto.PaginatedResponse;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.SolicitudInvalidaException;
import com.luadministra.exception.StockInsuficienteException;
import com.luadministra.materiaprima.MateriaPrima;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import com.luadministra.producto.Producto;
import com.luadministra.producto.ProductoRepository;
import com.luadministra.receta.RecetaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoteService {

    private final LoteRepository loteRepository;
    private final ProductoRepository productoRepository;
    private final RecetaRepository recetaRepository;
    private final MateriaPrimaRepository materiaPrimaRepository;

    public LoteService(LoteRepository loteRepository,
                       ProductoRepository productoRepository,
                       RecetaRepository recetaRepository,
                       MateriaPrimaRepository materiaPrimaRepository) {
        this.loteRepository = loteRepository;
        this.productoRepository = productoRepository;
        this.recetaRepository = recetaRepository;
        this.materiaPrimaRepository = materiaPrimaRepository;
    }

    public PaginatedResponse<LoteResponse> listar(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(sortDir != null && sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "fecha");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Lote> lotePage = loteRepository.findAll(pageable);
        List<LoteResponse> content = lotePage.stream().map(LoteResponse::fromEntity).toList();
        return PaginatedResponse.from(lotePage, content);
    }

    public PaginatedResponse<LoteResponse> listarPorPeriodo(LocalDate desde, LocalDate hasta, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"));
        Page<Lote> lotePage = loteRepository.findByFechaBetween(desde, hasta, pageable);
        List<LoteResponse> content = lotePage.stream().map(LoteResponse::fromEntity).toList();
        return PaginatedResponse.from(lotePage, content);
    }

    public LoteResponse obtener(Long id) {
        return LoteResponse.fromEntity(
                loteRepository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Lote no encontrado")));
    }

    @Transactional
    public LoteResponse crear(LoteRequest request) {
        Producto producto = productoRepository
                .findById(request.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));

        var receta = recetaRepository.findByProductoId(producto.getId())
                .orElseThrow(() -> new SolicitudInvalidaException("El producto no tiene una receta definida"));

        receta.getDetalles().forEach(d -> {
            MateriaPrima mp = d.getMateriaPrima();
            double cantidadNecesaria = d.getCantidad() * request.cantidadFabricada();
            if (mp.getStockActual() < cantidadNecesaria) {
                throw new StockInsuficienteException("Stock insuficiente de " + mp.getNombre());
            }
            mp.setStockActual(mp.getStockActual() - cantidadNecesaria);
            materiaPrimaRepository.save(mp);
        });

        producto.setStockActual(producto.getStockActual() + request.cantidadFabricada());
        productoRepository.save(producto);

        Lote lote = new Lote();
        lote.setProducto(producto);
        lote.setFecha(request.fecha());
        lote.setCantidadFabricada(request.cantidadFabricada());
        lote.setDiasVigencia(request.diasVigencia());

        return LoteResponse.fromEntity(loteRepository.save(lote));
    }

    public List<LoteResponse> listarLotesPorProducto(Long productoId) {
        return loteRepository.findByProductoIdOrderByFechaDesc(productoId)
                .stream()
                .map(LoteResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void eliminar(Long id) {
        Lote lote = loteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lote no encontrado"));

        Producto producto = lote.getProducto();
        var receta = recetaRepository.findByProductoId(producto.getId())
                .orElseThrow(() -> new SolicitudInvalidaException("La receta del producto ya no existe"));

        receta.getDetalles().forEach(d -> {
            MateriaPrima mp = d.getMateriaPrima();
            double cantidadADevolver = d.getCantidad() * lote.getCantidadFabricada();
            mp.setStockActual(mp.getStockActual() + cantidadADevolver);
            materiaPrimaRepository.save(mp);
        });

        producto.setStockActual(producto.getStockActual() - lote.getCantidadFabricada());
        productoRepository.save(producto);

        loteRepository.delete(lote);
    }
}
