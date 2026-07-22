package com.luadministra.colaboradora;

import com.luadministra.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColaboradoraService {

    private final ColaboradoraRepository repository;

    public ColaboradoraService(ColaboradoraRepository repository) {
        this.repository = repository;
    }

    public List<ColaboradoraResponse> listar() {
        return repository.findAll().stream()
                .map(ColaboradoraResponse::fromEntity)
                .toList();
    }

    public ColaboradoraResponse obtener(Long id) {
        return ColaboradoraResponse.fromEntity(
                repository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Colaboradora no encontrada")));
    }

    public ColaboradoraResponse crear(ColaboradoraRequest request) {
        Colaboradora c = new Colaboradora();
        c.setNombre(request.nombre());
        c.setContacto(request.contacto());
        return ColaboradoraResponse.fromEntity(repository.save(c));
    }

    public ColaboradoraResponse actualizar(Long id, ColaboradoraRequest request) {
        Colaboradora existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Colaboradora no encontrada"));
        existente.setNombre(request.nombre());
        existente.setContacto(request.contacto());
        return ColaboradoraResponse.fromEntity(repository.save(existente));
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
