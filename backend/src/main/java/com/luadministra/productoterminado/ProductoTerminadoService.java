package com.luadministra.productoterminado;

import com.luadministra.categoriaproductoterminado.CategoriaProductoTerminadoRepository;
import com.luadministra.despacho.DespachoService;
import com.luadministra.exception.RecursoNoEncontradoException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoTerminadoService {

    private final ProductoTerminadoRepository repository;
    private final CategoriaProductoTerminadoRepository categoriaRepository;
    private final DespachoService despachoService;

    public ProductoTerminadoService(ProductoTerminadoRepository repository,
                                    CategoriaProductoTerminadoRepository categoriaRepository,
                                    DespachoService despachoService) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.despachoService = despachoService;
    }

    public List<ProductoTerminadoResponse> listar(String nombre, Long categoriaId, String sortBy, String sortDir) {
        Sort sort = Sort.by(sortDir != null && sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "nombre");
        var stream = repository.findAll(sort).stream();
        if (nombre != null && !nombre.isBlank()) {
            stream = stream.filter(pt -> pt.getNombre().toLowerCase().contains(nombre.toLowerCase()));
        }
        if (categoriaId != null) {
            stream = stream.filter(pt -> { var c = pt.getCategoria(); return c != null && c.getId().equals(categoriaId); });
        }
        return stream.map(pt -> ProductoTerminadoResponse.fromEntity(pt,
                despachoService.calcularStockDespachado(pt.getId()))).toList();
    }

    public ProductoTerminadoResponse obtener(Long id) {
        ProductoTerminado pt = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto terminado no encontrado"));
        return ProductoTerminadoResponse.fromEntity(pt,
                despachoService.calcularStockDespachado(pt.getId()));
    }

    public ProductoTerminadoResponse crear(ProductoTerminadoRequest request) {
        ProductoTerminado pt = new ProductoTerminado();
        pt.setNombre(request.nombre());
        pt.setPrecioVenta(request.precioVenta());
        pt.setStockMinimo(request.stockMinimo());
        pt.setStockActual(0.0);
        if (request.categoriaId() != null) {
            pt.setCategoria(categoriaRepository.findById(request.categoriaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada")));
        }
        return ProductoTerminadoResponse.fromEntity(repository.save(pt));
    }

    public ProductoTerminadoResponse actualizar(Long id, ProductoTerminadoRequest request) {
        ProductoTerminado existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto terminado no encontrado"));
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
        return ProductoTerminadoResponse.fromEntity(repository.save(existente));
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
