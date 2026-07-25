package com.luadministra.dashboard;

import com.luadministra.lote.LoteRepository;
import com.luadministra.lote.LoteResponse;
import com.luadministra.materiaprima.MateriaPrimaResponse;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import com.luadministra.producto.Producto;
import com.luadministra.producto.ProductoRepository;
import com.luadministra.producto.ProductoResponse;
import com.luadministra.producto.ProductoService;
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
    private final ProductoRepository productoRepository;
    private final ProductoService productoService;
    private final LoteRepository loteRepository;

    public DashboardController(MateriaPrimaRepository materiaPrimaRepository,
                               ProductoRepository productoRepository,
                               ProductoService productoService,
                               LoteRepository loteRepository) {
        this.materiaPrimaRepository = materiaPrimaRepository;
        this.productoRepository = productoRepository;
        this.productoService = productoService;
        this.loteRepository = loteRepository;
    }

    @GetMapping("/stock")
    public Map<String, Object> stock() {
        List<MateriaPrimaResponse> materiasPrimas = materiaPrimaRepository.findAll().stream()
                .map(MateriaPrimaResponse::fromEntity)
                .toList();
        List<Producto> productos = productoRepository.findAll();
        List<ProductoResponse> productosResponse = productos.stream()
                .map(p -> ProductoResponse.fromEntity(p,
                        productoService.calcularStockConsignado(p.getId())))
                .toList();

        List<MateriaPrimaResponse> alertasMP = materiasPrimas.stream()
                .filter(mp -> { Double min = mp.stockMinimo(); return min != null && mp.stockActual() < min; })
                .toList();

        List<ProductoResponse> alertasPT = productosResponse.stream()
                .filter(p -> { Double min = p.stockMinimo(); return min != null && p.stockActual() < min; })
                .toList();

        List<LoteResponse> lotesVencidos = loteRepository
                .findByDiasVigenciaIsNotNull().stream()
                .filter(l -> l.getFecha().plusDays(l.getDiasVigencia()).isBefore(LocalDate.now()))
                .map(LoteResponse::fromEntity)
                .toList();

        List<Map<String, Object>> alertasVencimiento = lotesVencidos.stream()
                .filter(lv -> {
                    var p = productos.stream().filter(prod -> prod.getId().equals(lv.productoId())).findFirst();
                    return p.isPresent() && p.get().getStockActual() > 0;
                })
                .map(lv -> Map.<String, Object>of(
                        "loteId", lv.id(),
                        "productoId", lv.productoId(),
                        "productoNombre", lv.productoNombre(),
                        "fechaProduccion", lv.fecha().toString(),
                        "fechaVencimiento", lv.fechaVencimiento() != null ? lv.fechaVencimiento().toString() : null,
                        "cantidadFabricada", lv.cantidadFabricada()
                ))
                .toList();

        return Map.of(
                "materiasPrimas", materiasPrimas,
                "productosTerminados", productosResponse,
                "alertasMP", alertasMP,
                "alertasPT", alertasPT,
                "alertasVencimiento", alertasVencimiento
        );
    }
}
