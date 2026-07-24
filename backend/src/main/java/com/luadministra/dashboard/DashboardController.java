package com.luadministra.dashboard;

import com.luadministra.despacho.DespachoService;
import com.luadministra.materiaprima.MateriaPrimaResponse;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import com.luadministra.produccion.ProduccionRepository;
import com.luadministra.produccion.ProduccionResponse;
import com.luadministra.productoterminado.ProductoTerminado;
import com.luadministra.productoterminado.ProductoTerminadoRepository;
import com.luadministra.productoterminado.ProductoTerminadoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final MateriaPrimaRepository materiaPrimaRepository;
    private final ProductoTerminadoRepository productoTerminadoRepository;
    private final DespachoService despachoService;
    private final ProduccionRepository produccionRepository;

    public DashboardController(MateriaPrimaRepository materiaPrimaRepository,
                               ProductoTerminadoRepository productoTerminadoRepository,
                               DespachoService despachoService,
                               ProduccionRepository produccionRepository) {
        this.materiaPrimaRepository = materiaPrimaRepository;
        this.productoTerminadoRepository = productoTerminadoRepository;
        this.despachoService = despachoService;
        this.produccionRepository = produccionRepository;
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
                .filter(mp -> { Double min = mp.stockMinimo(); return min != null && mp.stockActual() < min; })
                .toList();

        List<ProductoTerminadoResponse> alertasPT = productosTerminados.stream()
                .filter(pt -> { Double min = pt.stockMinimo(); return min != null && pt.stockActual() < min; })
                .toList();

        List<ProduccionResponse> produccionesVencidas = produccionRepository
                .findByDiasVigenciaIsNotNull().stream()
                .filter(p -> p.getFecha().plusDays(p.getDiasVigencia()).isBefore(LocalDate.now()))
                .map(ProduccionResponse::fromEntity)
                .toList();

        List<Map<String, Object>> alertasVencimiento = produccionesVencidas.stream()
                .filter(pv -> {
                    var pt = pts.stream().filter(p -> p.getId().equals(pv.productoTerminadoId())).findFirst();
                    return pt.isPresent() && pt.get().getStockActual() > 0;
                })
                .map(pv -> Map.<String, Object>of(
                        "produccionId", pv.id(),
                        "productoTerminadoId", pv.productoTerminadoId(),
                        "productoTerminadoNombre", pv.productoTerminadoNombre(),
                        "fechaProduccion", pv.fecha().toString(),
                        "fechaVencimiento", pv.fechaVencimiento() != null ? pv.fechaVencimiento().toString() : null,
                        "cantidadFabricada", pv.cantidadFabricada()
                ))
                .toList();

        return Map.of(
                "materiasPrimas", materiasPrimas,
                "productosTerminados", productosTerminados,
                "alertasMP", alertasMP,
                "alertasPT", alertasPT,
                "alertasVencimiento", alertasVencimiento
        );
    }
}
