package com.luadministra.producto;

import com.luadministra.categoria.CategoriaRepository;
import com.luadministra.consignacion.ConsignacionRepository;
import com.luadministra.consignacion.EstadoConsignacion;
import com.luadministra.dto.PaginatedResponse;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.SolicitudInvalidaException;
import com.luadministra.lote.LoteRepository;
import com.luadministra.receta.RecetaDetalleRepository;
import com.luadministra.venta.VentaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final ConsignacionRepository consignacionRepository;
    private final RecetaDetalleRepository recetaDetalleRepository;
    private final LoteRepository loteRepository;
    private final VentaRepository ventaRepository;

    public ProductoService(ProductoRepository repository,
                           CategoriaRepository categoriaRepository,
                           ConsignacionRepository consignacionRepository,
                           RecetaDetalleRepository recetaDetalleRepository,
                           LoteRepository loteRepository,
                           VentaRepository ventaRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.consignacionRepository = consignacionRepository;
        this.recetaDetalleRepository = recetaDetalleRepository;
        this.loteRepository = loteRepository;
        this.ventaRepository = ventaRepository;
    }

    public List<ProductoResponse> listar(String nombre, Long categoriaId, String sortBy, String sortDir) {
        Specification<Producto> spec = Specification
                .where(ProductoSpecification.nombreContains(nombre))
                .and(ProductoSpecification.categoriaIdEquals(categoriaId));

        Sort sort = Sort.by(sortDir != null && sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "nombre");

        return repository.findAll(spec, sort).stream()
                .map(p -> ProductoResponse.fromEntity(p, calcularStockConsignado(p.getId())))
                .toList();
    }

    public PaginatedResponse<ProductoResponse> listarPaginado(int page, int size, String nombre, Long categoriaId, String sortBy, String sortDir) {
        Specification<Producto> spec = Specification
                .where(ProductoSpecification.nombreContains(nombre))
                .and(ProductoSpecification.categoriaIdEquals(categoriaId));

        Sort sort = Sort.by(sortDir != null && sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "nombre");

        Page<Producto> result = repository.findAll(spec, PageRequest.of(page, size, sort));
        List<ProductoResponse> content = result.getContent().stream()
                .map(p -> ProductoResponse.fromEntity(p, calcularStockConsignado(p.getId())))
                .toList();
        return PaginatedResponse.from(result, content);
    }

    public ProductoResponse obtener(Long id) {
        Producto p = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        return ProductoResponse.fromEntity(p, calcularStockConsignado(p.getId()));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Producto p = new Producto();
        Double stockActual = request.stockActual();
        p.setNombre(request.nombre());
        p.setPrecioVenta(request.precioVenta());
        p.setStockMinimo(request.stockMinimo());
        p.setStockActual(stockActual != null ? stockActual : 0.0);

        if (request.categoriaId() != null) {
            p.setCategoria(categoriaRepository.findById(request.categoriaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada")));
        }
        return ProductoResponse.fromEntity(repository.save(p));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        existente.setNombre(request.nombre());
        existente.setPrecioVenta(request.precioVenta());
        existente.setStockMinimo(request.stockMinimo());
        if (request.stockActual() != null) {
            existente.setStockActual(request.stockActual());
        }
        if (request.categoriaId() != null) {
            existente.setCategoria(categoriaRepository.findById(request.categoriaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada")));
        } else {
            existente.setCategoria(null);
        }
        return ProductoResponse.fromEntity(repository.save(existente));
    }

    @Transactional
    public void eliminar(Long id) {
        long recetas = recetaDetalleRepository.countRecetasByProductoId(id);
        long lotes = loteRepository.countByProductoId(id);
        long ventas = ventaRepository.countLineasByProductoId(id);
        long consignaciones = consignacionRepository.countLineasByProductoId(id);
        if (recetas > 0 || lotes > 0 || ventas > 0 || consignaciones > 0) {
            throw new SolicitudInvalidaException("No se puede eliminar: está en uso en " + recetas
                    + " receta(s), " + lotes + " lote(s), " + ventas + " venta(s) y "
                    + consignaciones + " consignación(es)");
        }
        repository.deleteById(id);
    }

    public Double calcularStockConsignado(Long productoId) {
        List<com.luadministra.consignacion.LineaConsignacion> activas = consignacionRepository
                .findLineasByProductoIdAndEstadoNot(productoId, EstadoConsignacion.RENDIDO_TOTAL);
        return activas.stream()
                .mapToDouble(lc -> lc.getCantidad())
                .sum();
    }
}
