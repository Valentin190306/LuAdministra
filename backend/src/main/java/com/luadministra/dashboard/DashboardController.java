package com.luadministra.dashboard;

import com.luadministra.despacho.DespachoService;
import com.luadministra.materiaprima.MateriaPrimaResponse;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import com.luadministra.productoterminado.ProductoTerminado;
import com.luadministra.productoterminado.ProductoTerminadoRepository;
import com.luadministra.productoterminado.ProductoTerminadoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final MateriaPrimaRepository materiaPrimaRepository;
    private final ProductoTerminadoRepository productoTerminadoRepository;
    private final DespachoService despachoService;

    public DashboardController(MateriaPrimaRepository materiaPrimaRepository,
                               ProductoTerminadoRepository productoTerminadoRepository,
                               DespachoService despachoService) {
        this.materiaPrimaRepository = materiaPrimaRepository;
        this.productoTerminadoRepository = productoTerminadoRepository;
        this.despachoService = despachoService;
    }

    @GetMapping("/stock")
    public Map<String, Object> stock() {
        List<MateriaPrimaResponse> materiasPrimas = materiaPrimaRepository.findAll().stream()
                .map(MateriaPrimaResponse::fromEntity)
                .toList();
        List<ProductoTerminado> pts = productoTerminadoRepository.findAll();
        List<ProductoTerminadoResponse> productosTerminados = pts.stream()
                .map(pt -> ProductoTerminadoResponse.fromEntity(pt,
                        despachoService.calcularStockDespachado(pt.getId())))
                .toList();

        List<MateriaPrimaResponse> alertasMP = materiasPrimas.stream()
                .filter(mp -> mp.stockMinimo() != null && mp.stockActual() < mp.stockMinimo())
                .toList();

        List<ProductoTerminadoResponse> alertasPT = productosTerminados.stream()
                .filter(pt -> pt.stockMinimo() != null && pt.stockActual() < pt.stockMinimo())
                .toList();

        return Map.of(
                "materiasPrimas", materiasPrimas,
                "productosTerminados", productosTerminados,
                "alertasMP", alertasMP,
                "alertasPT", alertasPT
        );
    }
}
