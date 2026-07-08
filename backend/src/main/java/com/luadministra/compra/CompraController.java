package com.luadministra.compra;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final CompraService service;

    public CompraController(CompraService service) {
        this.service = service;
    }

    @GetMapping
    public List<CompraResponse> listar() {
        return service.listar();
    }

    @GetMapping("/materia-prima/{materiaPrimaId}")
    public List<CompraResponse> listarPorMateriaPrima(@PathVariable Long materiaPrimaId) {
        return service.listarPorMateriaPrima(materiaPrimaId);
    }

    @GetMapping("/{id}")
    public CompraResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public CompraResponse crear(@Valid @RequestBody CompraRequest request) {
        return service.crear(request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
