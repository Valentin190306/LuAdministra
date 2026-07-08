package com.luadministra.materiaprima;

import com.luadministra.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MateriaPrimaService {

    private final MateriaPrimaRepository repository;

    public MateriaPrimaService(MateriaPrimaRepository repository) {
        this.repository = repository;
    }

    public List<MateriaPrimaResponse> listar() {
        return repository.findAll().stream()
                .map(MateriaPrimaResponse::fromEntity)
                .toList();
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
        return MateriaPrimaResponse.fromEntity(repository.save(mp));
    }

    public MateriaPrimaResponse actualizar(Long id, MateriaPrimaRequest request) {
        MateriaPrima existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Materia prima no encontrada"));
        existente.setNombre(request.nombre());
        existente.setUnidadMedida(request.unidadMedida());
        existente.setStockMinimo(request.stockMinimo());
        return MateriaPrimaResponse.fromEntity(repository.save(existente));
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
