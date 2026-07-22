package com.luadministra.compra;

import com.luadministra.dto.PaginatedResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final CompraService service;

    public CompraController(CompraService service) {
        this.service = service;
    }

    @GetMapping
    public PaginatedResponse<CompraResponse> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {
        return service.listar(page, size, sortBy, sortDir);
    }

    @GetMapping("/materia-prima/{materiaPrimaId}")
    public PaginatedResponse<CompraResponse> listarPorMateriaPrima(
            @PathVariable Long materiaPrimaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {
        return service.listarPorMateriaPrima(materiaPrimaId, page, size, sortBy, sortDir);
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
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
