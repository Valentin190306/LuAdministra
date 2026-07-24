package com.luadministra.despacho;

import com.luadministra.dto.PaginatedResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/despachos")
public class DespachoController {

    private final DespachoService service;

    public DespachoController(DespachoService service) {
        this.service = service;
    }

    @GetMapping
    public PaginatedResponse<DespachoResponse> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) Long colaboradoraId,
            @RequestParam(required = false) String estado) {
        return service.listar(page, size, sortBy, sortDir, colaboradoraId, estado);
    }

    @GetMapping("/{id}")
    public DespachoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public DespachoResponse crear(@Valid @RequestBody DespachoRequest request) {
        return service.crear(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stock-consignado")
    public List<StockConsignadoResponse> stockConsignado(@RequestParam Long colaboradoraId) {
        return service.stockConsignado(colaboradoraId);
    }
}
