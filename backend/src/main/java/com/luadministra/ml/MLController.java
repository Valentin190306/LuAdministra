package com.luadministra.ml;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MLController {

    private final MLService mlService;

    public MLController(MLService mlService) {
        this.mlService = mlService;
    }

    @PostMapping("/compras/{id}/consultar-precio-ml")
    public ConsultaPrecioMLResponse consultarPrecio(@PathVariable Long id) {
        return mlService.consultarPrecio(id);
    }

    @GetMapping("/compras/{id}/consultas-ml")
    public List<ConsultaPrecioMLResponse> listarConsultas(@PathVariable Long id) {
        return mlService.listarConsultas(id);
    }

    @GetMapping("/ml/compras-con-ml")
    public List<CompraConMLResponse> listarComprasConML() {
        return mlService.listarComprasConML();
    }
}
