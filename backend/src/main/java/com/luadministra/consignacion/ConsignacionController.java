package com.luadministra.consignacion;

import com.luadministra.dto.PaginatedResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consignaciones")
public class ConsignacionController {

    private final ConsignacionService service;

    public ConsignacionController(ConsignacionService service) {
        this.service = service;
    }

    @GetMapping
    public PaginatedResponse<ConsignacionResponse> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) Long consignatarioId,
            @RequestParam(required = false) String estado) {
        return service.listar(page, size, sortBy, sortDir, consignatarioId, estado);
    }

    @GetMapping("/{id}")
    public ConsignacionResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ConsignacionResponse crear(@Valid @RequestBody ConsignacionRequest request) {
        return service.crear(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stock-consignado")
    public List<StockConsignadoResponse> stockConsignado(@RequestParam Long consignatarioId) {
        return service.stockConsignado(consignatarioId);
    }
}
