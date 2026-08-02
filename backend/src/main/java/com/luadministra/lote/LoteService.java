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
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
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
        boolean desc = sortDir != null && sortDir.equalsIgnoreCase("desc");

        if ("fechaVencimiento".equals(sortBy)) {
            List<LoteResponse> ordenados = loteRepository.findAll().stream()
                    .map(LoteResponse::fromEntity)
                    .sorted(comparadorPorVencimiento(desc))
                    .toList();
            int from = Math.min(page * size, ordenados.size());
            int to = Math.min(from + size, ordenados.size());
            List<LoteResponse> content = ordenados.subList(from, to);
            int totalPages = (int) Math.ceil((double) ordenados.size() / size);
            return new PaginatedResponse<>(content, page, size, ordenados.size(), totalPages);
        }

        Sort sort = Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "fecha");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Lote> lotePage = loteRepository.findAll(pageable);
        List<LoteResponse> content = lotePage.stream().map(LoteResponse::fromEntity).toList();
        return PaginatedResponse.from(lotePage, content);
    }

    private Comparator<LoteResponse> comparadorPorVencimiento(boolean desc) {
        Comparator<LocalDate> fechas = desc
                ? Comparator.nullsLast(Comparator.reverseOrder())
                : Comparator.nullsLast(Comparator.naturalOrder());
        return (lote1, lote2) -> {
            LocalDate fecha1 = lote1 != null ? lote1.fechaVencimiento() : null;
            LocalDate fecha2 = lote2 != null ? lote2.fechaVencimiento() : null;
            return fechas.compare(fecha1, fecha2);
        };
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
