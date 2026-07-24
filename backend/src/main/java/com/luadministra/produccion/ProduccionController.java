package com.luadministra.produccion;

import com.luadministra.dto.PaginatedResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/producciones")
public class ProduccionController {

    private final ProduccionService service;

    public ProduccionController(ProduccionService service) {
        this.service = service;
    }

    @GetMapping
    public PaginatedResponse<ProduccionResponse> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {
        return service.listar(page, size, sortBy, sortDir);
    }

    @GetMapping("/periodo")
    public PaginatedResponse<ProduccionResponse> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return service.listarPorPeriodo(desde, hasta, page, size);
    }

    @GetMapping("/{id}")
    public ProduccionResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ProduccionResponse crear(@Valid @RequestBody ProduccionRequest request) {
        return service.crear(request);
    }

    @GetMapping("/lotes")
    public List<ProduccionResponse> listarLotes(@RequestParam Long productoId) {
        return service.listarLotesPorProducto(productoId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
