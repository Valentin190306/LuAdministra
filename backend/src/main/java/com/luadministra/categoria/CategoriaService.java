package com.luadministra.categoria;

import com.luadministra.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public List<CategoriaResponse> listar(TipoCategoria tipo) {
        if (tipo != null) {
            return repository.findByTipo(tipo).stream()
                    .map(CategoriaResponse::fromEntity)
                    .toList();
        }
        return repository.findAll().stream()
                .map(CategoriaResponse::fromEntity)
                .toList();
    }

    public CategoriaResponse obtener(Long id) {
        return CategoriaResponse.fromEntity(
                repository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada")));
    }

    public CategoriaResponse crear(CategoriaRequest request) {
        Categoria categoria = new Categoria();
        categoria.setNombre(request.nombre());
        categoria.setTipo(request.tipo());
        if (request.categoriaPadreId() != null) {
            categoria.setCategoriaPadre(repository.findById(request.categoriaPadreId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoria padre no encontrada")));
        }
        return CategoriaResponse.fromEntity(repository.save(categoria));
    }

    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada"));
        existente.setNombre(request.nombre());
        existente.setTipo(request.tipo());
        if (request.categoriaPadreId() != null) {
            existente.setCategoriaPadre(repository.findById(request.categoriaPadreId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoria padre no encontrada")));
        } else {
            existente.setCategoriaPadre(null);
        }
        return CategoriaResponse.fromEntity(repository.save(existente));
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
