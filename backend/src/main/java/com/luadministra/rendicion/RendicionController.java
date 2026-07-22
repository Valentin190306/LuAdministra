package com.luadministra.rendicion;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RendicionController {

    private final RendicionService service;

    public RendicionController(RendicionService service) {
        this.service = service;
    }

    @GetMapping("/despachos/{despachoId}/rendiciones")
    public List<RendicionResponse> listarPorDespacho(@PathVariable Long despachoId) {
        return service.listarPorDespacho(despachoId);
    }

    @PostMapping("/rendiciones")
    public RendicionResponse crear(@Valid @RequestBody RendicionRequest request) {
        return service.crear(request);
    }
}
