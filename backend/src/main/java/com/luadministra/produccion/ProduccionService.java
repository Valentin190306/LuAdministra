package com.luadministra.produccion;

import com.luadministra.dto.PaginatedResponse;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.SolicitudInvalidaException;
import com.luadministra.exception.StockInsuficienteException;
import com.luadministra.materiaprima.MateriaPrima;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import com.luadministra.productoterminado.ProductoTerminado;
import com.luadministra.productoterminado.ProductoTerminadoRepository;
import com.luadministra.receta.Receta;
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
public class ProduccionService {

    private final ProduccionRepository produccionRepository;
    private final ProductoTerminadoRepository productoTerminadoRepository;
    private final RecetaRepository recetaRepository;
    private final MateriaPrimaRepository materiaPrimaRepository;

    public ProduccionService(ProduccionRepository produccionRepository,
                             ProductoTerminadoRepository productoTerminadoRepository,
                             RecetaRepository recetaRepository,
                             MateriaPrimaRepository materiaPrimaRepository) {
        this.produccionRepository = produccionRepository;
        this.productoTerminadoRepository = productoTerminadoRepository;
        this.recetaRepository = recetaRepository;
        this.materiaPrimaRepository = materiaPrimaRepository;
    }

    public PaginatedResponse<ProduccionResponse> listar(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(sortDir != null && sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "fecha");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Produccion> produccionPage = produccionRepository.findAll(pageable);
        List<ProduccionResponse> content = produccionPage.stream().map(ProduccionResponse::fromEntity).toList();
        return PaginatedResponse.from(produccionPage, content);
    }

    public PaginatedResponse<ProduccionResponse> listarPorPeriodo(LocalDate desde, LocalDate hasta, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"));
        Page<Produccion> produccionPage = produccionRepository.findByFechaBetween(desde, hasta, pageable);
        List<ProduccionResponse> content = produccionPage.stream().map(ProduccionResponse::fromEntity).toList();
        return PaginatedResponse.from(produccionPage, content);
    }

    public ProduccionResponse obtener(Long id) {
        return ProduccionResponse.fromEntity(
                produccionRepository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Produccion no encontrada")));
    }

    @Transactional
    public ProduccionResponse crear(ProduccionRequest request) {
        ProductoTerminado pt = productoTerminadoRepository
                .findById(request.productoTerminadoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto terminado no encontrado"));

        Receta receta = recetaRepository.findByProductoTerminadoId(pt.getId())
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

        pt.setStockActual(pt.getStockActual() + request.cantidadFabricada());
        productoTerminadoRepository.save(pt);

        Produccion produccion = new Produccion();
        produccion.setProductoTerminado(pt);
        produccion.setFecha(request.fecha());
        produccion.setCantidadFabricada(request.cantidadFabricada());
        produccion.setDiasVigencia(request.diasVigencia());

        return ProduccionResponse.fromEntity(produccionRepository.save(produccion));
    }

    public List<ProduccionResponse> listarLotesPorProducto(Long productoTerminadoId) {
        return produccionRepository.findByProductoTerminadoIdOrderByFechaDesc(productoTerminadoId)
                .stream()
                .map(ProduccionResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void eliminar(Long id) {
        Produccion produccion = produccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Produccion no encontrada"));

        ProductoTerminado pt = produccion.getProductoTerminado();
        Receta receta = recetaRepository.findByProductoTerminadoId(pt.getId())
                .orElseThrow(() -> new SolicitudInvalidaException("La receta del producto ya no existe"));

        receta.getDetalles().forEach(d -> {
            MateriaPrima mp = d.getMateriaPrima();
            double cantidadADevolver = d.getCantidad() * produccion.getCantidadFabricada();
            mp.setStockActual(mp.getStockActual() + cantidadADevolver);
            materiaPrimaRepository.save(mp);
        });

        pt.setStockActual(pt.getStockActual() - produccion.getCantidadFabricada());
        productoTerminadoRepository.save(pt);

        produccionRepository.delete(produccion);
    }
}
