package com.luadministra.producto;

import com.luadministra.categoria.CategoriaRepository;
import com.luadministra.consignacion.ConsignacionRepository;
import com.luadministra.consignacion.EstadoConsignacion;
import com.luadministra.exception.RecursoNoEncontradoException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final ConsignacionRepository consignacionRepository;

    public ProductoService(ProductoRepository repository,
                           CategoriaRepository categoriaRepository,
                           ConsignacionRepository consignacionRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.consignacionRepository = consignacionRepository;
    }

    public List<ProductoResponse> listar(String nombre, Long categoriaId, String sortBy, String sortDir) {
        Sort sort = Sort.by(sortDir != null && sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "nombre");
        var stream = repository.findAll(sort).stream();
        if (nombre != null && !nombre.isBlank()) {
            stream = stream.filter(p -> p.getNombre().toLowerCase().contains(nombre.toLowerCase()));
        }
        if (categoriaId != null) {
            stream = stream.filter(p -> { var c = p.getCategoria(); return c != null && c.getId().equals(categoriaId); });
        }
        return stream.map(p -> ProductoResponse.fromEntity(p,
                calcularStockConsignado(p.getId()))).toList();
    }

    public ProductoResponse obtener(Long id) {
        Producto p = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        return ProductoResponse.fromEntity(p, calcularStockConsignado(p.getId()));
    }

    public ProductoResponse crear(ProductoRequest request) {
        Producto p = new Producto();
        p.setNombre(request.nombre());
        p.setPrecioVenta(request.precioVenta());
        p.setStockMinimo(request.stockMinimo());
        p.setStockActual(0.0);
        if (request.categoriaId() != null) {
            p.setCategoria(categoriaRepository.findById(request.categoriaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada")));
        }
        return ProductoResponse.fromEntity(repository.save(p));
    }

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

    public void eliminar(Long id) {
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
