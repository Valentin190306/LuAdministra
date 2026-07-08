package com.luadministra.produccion;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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
    public List<ProduccionResponse> listar() {
        return service.listar();
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
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
