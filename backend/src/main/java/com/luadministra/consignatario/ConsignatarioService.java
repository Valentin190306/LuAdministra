package com.luadministra.consignatario;

import com.luadministra.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsignatarioService {

    private final ConsignatarioRepository repository;

    public ConsignatarioService(ConsignatarioRepository repository) {
        this.repository = repository;
    }

    public List<ConsignatarioResponse> listar() {
        return repository.findAll().stream()
                .map(ConsignatarioResponse::fromEntity)
                .toList();
    }

    public ConsignatarioResponse obtener(Long id) {
        return ConsignatarioResponse.fromEntity(
                repository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Consignatario no encontrado")));
    }

    public ConsignatarioResponse crear(ConsignatarioRequest request) {
        Consignatario c = new Consignatario();
        c.setNombre(request.nombre());
        c.setContacto(request.contacto());
        return ConsignatarioResponse.fromEntity(repository.save(c));
    }

    public ConsignatarioResponse actualizar(Long id, ConsignatarioRequest request) {
        Consignatario existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Consignatario no encontrado"));
        existente.setNombre(request.nombre());
        existente.setContacto(request.contacto());
        return ConsignatarioResponse.fromEntity(repository.save(existente));
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
