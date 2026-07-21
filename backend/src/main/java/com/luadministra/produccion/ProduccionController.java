package com.luadministra.produccion;

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
    public List<ProduccionResponse> listar(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {
        return service.listar(sortBy, sortDir);
    }

    @GetMapping("/periodo")
    public List<ProduccionResponse> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.listarPorPeriodo(desde, hasta);
    }

    @GetMapping("/{id}")
    public ProduccionResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ProduccionResponse crear(@Valid @RequestBody ProduccionRequest request) {
        return service.crear(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
