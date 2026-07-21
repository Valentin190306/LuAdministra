package com.luadministra.categoriamateriaprima;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias-materias-primas")
public class CategoriaMateriaPrimaController {

    private final CategoriaMateriaPrimaService service;

    public CategoriaMateriaPrimaController(CategoriaMateriaPrimaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoriaMateriaPrimaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public CategoriaMateriaPrimaResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public CategoriaMateriaPrimaResponse crear(@Valid @RequestBody CategoriaMateriaPrimaRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public CategoriaMateriaPrimaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaMateriaPrimaRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
