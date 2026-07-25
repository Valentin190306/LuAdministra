package com.luadministra.rendicion;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rendiciones")
public class RendicionController {

    private final RendicionService service;

    public RendicionController(RendicionService service) {
        this.service = service;
    }

    @GetMapping("/consignacion/{consignacionId}")
    public List<RendicionResponse> listarPorConsignacion(@PathVariable Long consignacionId) {
        return service.listarPorConsignacion(consignacionId);
    }

    @PostMapping
    public RendicionResponse crear(@Valid @RequestBody RendicionRequest request) {
        return service.crear(request);
    }
}
