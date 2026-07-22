package com.luadministra.venta;

import com.luadministra.dto.PaginatedResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService service;

    public VentaController(VentaService service) {
        this.service = service;
    }

    @GetMapping
    public PaginatedResponse<VentaResponse> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {
        return service.listar(page, size, sortBy, sortDir);
    }

    @GetMapping("/periodo")
    public PaginatedResponse<VentaResponse> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return service.listarPorPeriodo(desde, hasta, page, size);
    }

    @GetMapping("/{id}")
    public VentaResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public VentaResponse crear(@Valid @RequestBody VentaRequest request) {
        return service.crear(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
