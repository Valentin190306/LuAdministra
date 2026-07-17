package com.luadministra.materiaprima;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materias-primas")
public class MateriaPrimaController {

    private final MateriaPrimaService service;

    public MateriaPrimaController(MateriaPrimaService service) {
        this.service = service;
    }

    @GetMapping
    public List<MateriaPrimaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public MateriaPrimaResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public MateriaPrimaResponse crear(@Valid @RequestBody MateriaPrimaRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public MateriaPrimaResponse actualizar(@PathVariable Long id, @Valid @RequestBody MateriaPrimaRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
