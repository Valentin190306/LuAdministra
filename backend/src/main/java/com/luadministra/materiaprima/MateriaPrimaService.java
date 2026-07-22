package com.luadministra.materiaprima;

import com.luadministra.categoriamateriaprima.CategoriaMateriaPrimaRepository;
import com.luadministra.exception.RecursoNoEncontradoException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class MateriaPrimaService {

    private final MateriaPrimaRepository repository;
    private final CategoriaMateriaPrimaRepository categoriaRepository;

    public MateriaPrimaService(MateriaPrimaRepository repository, CategoriaMateriaPrimaRepository categoriaRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<MateriaPrimaResponse> listar(String nombre, Long categoriaId, String sortBy, String sortDir) {
        Sort sort = Sort.by(sortDir != null && sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "nombre");
        var stream = repository.findAll(sort).stream();
        if (nombre != null && !nombre.isBlank()) {
            stream = stream.filter(mp -> mp.getNombre().toLowerCase().contains(nombre.toLowerCase()));
        }
        if (categoriaId != null) {
            stream = stream.filter(mp -> mp.getCategoria() != null && mp.getCategoria().getId().equals(categoriaId));
        }
        return stream.map(MateriaPrimaResponse::fromEntity).toList();
    }

    public MateriaPrimaResponse obtener(Long id) {
        return MateriaPrimaResponse.fromEntity(
                repository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Materia prima no encontrada")));
    }

    public MateriaPrimaResponse crear(MateriaPrimaRequest request) {
        MateriaPrima mp = new MateriaPrima();
        mp.setNombre(request.nombre());
        mp.setUnidadMedida(request.unidadMedida());
        mp.setStockMinimo(request.stockMinimo());
        mp.setStockActual(0.0);
        if (request.categoriaId() != null) {
            mp.setCategoria(categoriaRepository.findById(request.categoriaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada")));
        }
        return MateriaPrimaResponse.fromEntity(repository.save(mp));
    }

    public MateriaPrimaResponse actualizar(Long id, MateriaPrimaRequest request) {
        MateriaPrima existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Materia prima no encontrada"));
        existente.setNombre(request.nombre());
        existente.setUnidadMedida(request.unidadMedida());
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
        return MateriaPrimaResponse.fromEntity(repository.save(existente));
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
