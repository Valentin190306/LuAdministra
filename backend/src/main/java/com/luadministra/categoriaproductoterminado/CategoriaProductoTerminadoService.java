package com.luadministra.categoriaproductoterminado;

import com.luadministra.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaProductoTerminadoService {

    private final CategoriaProductoTerminadoRepository repository;

    public CategoriaProductoTerminadoService(CategoriaProductoTerminadoRepository repository) {
        this.repository = repository;
    }

    public List<CategoriaProductoTerminadoResponse> listar() {
        return repository.findAll().stream()
                .map(CategoriaProductoTerminadoResponse::fromEntity)
                .toList();
    }

    public CategoriaProductoTerminadoResponse obtener(Long id) {
        return CategoriaProductoTerminadoResponse.fromEntity(
                repository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Categoria de producto terminado no encontrada")));
    }

    public CategoriaProductoTerminadoResponse crear(CategoriaProductoTerminadoRequest request) {
        CategoriaProductoTerminado categoria = new CategoriaProductoTerminado();
        categoria.setNombre(request.nombre());
        if (request.categoriaPadreId() != null) {
            categoria.setCategoriaPadre(repository.findById(request.categoriaPadreId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoria padre no encontrada")));
        }
        return CategoriaProductoTerminadoResponse.fromEntity(repository.save(categoria));
    }

    public CategoriaProductoTerminadoResponse actualizar(Long id, CategoriaProductoTerminadoRequest request) {
        CategoriaProductoTerminado existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria de producto terminado no encontrada"));
        existente.setNombre(request.nombre());
        if (request.categoriaPadreId() != null) {
            existente.setCategoriaPadre(repository.findById(request.categoriaPadreId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoria padre no encontrada")));
        } else {
            existente.setCategoriaPadre(null);
        }
        return CategoriaProductoTerminadoResponse.fromEntity(repository.save(existente));
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
