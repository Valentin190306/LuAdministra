package com.luadministra.productoterminado;

import com.luadministra.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoTerminadoService {

    private final ProductoTerminadoRepository repository;

    public ProductoTerminadoService(ProductoTerminadoRepository repository) {
        this.repository = repository;
    }

    public List<ProductoTerminadoResponse> listar() {
        return repository.findAll().stream()
                .map(ProductoTerminadoResponse::fromEntity)
                .toList();
    }

    public ProductoTerminadoResponse obtener(Long id) {
        return ProductoTerminadoResponse.fromEntity(
                repository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Producto terminado no encontrado")));
    }

    public ProductoTerminadoResponse crear(ProductoTerminadoRequest request) {
        ProductoTerminado pt = new ProductoTerminado();
        pt.setNombre(request.nombre());
        pt.setPrecioVenta(request.precioVenta());
        pt.setStockMinimo(request.stockMinimo());
        pt.setStockActual(0.0);
        return ProductoTerminadoResponse.fromEntity(repository.save(pt));
    }

    public ProductoTerminadoResponse actualizar(Long id, ProductoTerminadoRequest request) {
        ProductoTerminado existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto terminado no encontrado"));
        existente.setNombre(request.nombre());
        existente.setPrecioVenta(request.precioVenta());
        existente.setStockMinimo(request.stockMinimo());
        return ProductoTerminadoResponse.fromEntity(repository.save(existente));
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
