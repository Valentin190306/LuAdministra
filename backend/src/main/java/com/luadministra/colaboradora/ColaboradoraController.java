package com.luadministra.colaboradora;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colaboradoras")
public class ColaboradoraController {

    private final ColaboradoraService service;

    public ColaboradoraController(ColaboradoraService service) {
        this.service = service;
    }

    @GetMapping
    public List<ColaboradoraResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ColaboradoraResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ColaboradoraResponse crear(@Valid @RequestBody ColaboradoraRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public ColaboradoraResponse actualizar(@PathVariable Long id, @Valid @RequestBody ColaboradoraRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
