package com.luadministra.categoriaproductoterminado;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias-productos-terminados")
public class CategoriaProductoTerminadoController {

    private final CategoriaProductoTerminadoService service;

    public CategoriaProductoTerminadoController(CategoriaProductoTerminadoService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoriaProductoTerminadoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public CategoriaProductoTerminadoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public CategoriaProductoTerminadoResponse crear(@Valid @RequestBody CategoriaProductoTerminadoRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public CategoriaProductoTerminadoResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaProductoTerminadoRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
