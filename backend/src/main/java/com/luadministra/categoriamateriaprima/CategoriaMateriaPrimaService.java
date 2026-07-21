package com.luadministra.categoriamateriaprima;

import com.luadministra.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaMateriaPrimaService {

    private final CategoriaMateriaPrimaRepository repository;

    public CategoriaMateriaPrimaService(CategoriaMateriaPrimaRepository repository) {
        this.repository = repository;
    }

    public List<CategoriaMateriaPrimaResponse> listar() {
        return repository.findAll().stream()
                .map(CategoriaMateriaPrimaResponse::fromEntity)
                .toList();
    }

    public CategoriaMateriaPrimaResponse obtener(Long id) {
        return CategoriaMateriaPrimaResponse.fromEntity(
                repository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Categoria de materia prima no encontrada")));
    }

    public CategoriaMateriaPrimaResponse crear(CategoriaMateriaPrimaRequest request) {
        CategoriaMateriaPrima categoria = new CategoriaMateriaPrima();
        categoria.setNombre(request.nombre());
        if (request.categoriaPadreId() != null) {
            categoria.setCategoriaPadre(repository.findById(request.categoriaPadreId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoria padre no encontrada")));
        }
        return CategoriaMateriaPrimaResponse.fromEntity(repository.save(categoria));
    }

    public CategoriaMateriaPrimaResponse actualizar(Long id, CategoriaMateriaPrimaRequest request) {
        CategoriaMateriaPrima existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria de materia prima no encontrada"));
        existente.setNombre(request.nombre());
        if (request.categoriaPadreId() != null) {
            existente.setCategoriaPadre(repository.findById(request.categoriaPadreId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoria padre no encontrada")));
        } else {
            existente.setCategoriaPadre(null);
        }
        return CategoriaMateriaPrimaResponse.fromEntity(repository.save(existente));
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
