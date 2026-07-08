package com.luadministra.productoterminado;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos-terminados")
public class ProductoTerminadoController {

    private final ProductoTerminadoService service;

    public ProductoTerminadoController(ProductoTerminadoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductoTerminadoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ProductoTerminadoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ProductoTerminadoResponse crear(@Valid @RequestBody ProductoTerminadoRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public ProductoTerminadoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoTerminadoRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
