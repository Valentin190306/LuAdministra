package com.luadministra.receta;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recetas")
public class RecetaController {

    private final RecetaService service;

    public RecetaController(RecetaService service) {
        this.service = service;
    }

    @GetMapping
    public List<RecetaResponse> listarTodas() {
        return service.obtenerTodas();
    }

    @GetMapping("/producto/{productoTerminadoId}")
    public RecetaResponse obtenerPorProducto(@PathVariable Long productoTerminadoId) {
        return service.obtenerPorProducto(productoTerminadoId);
    }

    @PostMapping
    public RecetaResponse crear(@Valid @RequestBody RecetaRequest request) {
        return service.guardar(request);
    }

    @PutMapping("/{id}")
    public RecetaResponse actualizar(@PathVariable Long id, @Valid @RequestBody RecetaRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
