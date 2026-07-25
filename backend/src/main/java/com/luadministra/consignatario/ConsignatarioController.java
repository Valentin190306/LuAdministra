package com.luadministra.consignatario;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consignatarios")
public class ConsignatarioController {

    private final ConsignatarioService service;

    public ConsignatarioController(ConsignatarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<ConsignatarioResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ConsignatarioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ConsignatarioResponse crear(@Valid @RequestBody ConsignatarioRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public ConsignatarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ConsignatarioRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
