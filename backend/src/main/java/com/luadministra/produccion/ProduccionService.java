package com.luadministra.produccion;

import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.SolicitudInvalidaException;
import com.luadministra.exception.StockInsuficienteException;
import com.luadministra.materiaprima.MateriaPrima;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import com.luadministra.productoterminado.ProductoTerminado;
import com.luadministra.productoterminado.ProductoTerminadoRepository;
import com.luadministra.receta.Receta;
import com.luadministra.receta.RecetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProduccionService {

    private final ProduccionRepository produccionRepository;
    private final ProductoTerminadoRepository productoTerminadoRepository;
    private final RecetaRepository recetaRepository;
    private final MateriaPrimaRepository materiaPrimaRepository;

    public ProduccionService(ProduccionRepository produccionRepository,
                             ProductoTerminadoRepository productoTerminadoRepository,
                             RecetaRepository recetaRepository,
                             MateriaPrimaRepository materiaPrimaRepository) {
        this.produccionRepository = produccionRepository;
        this.productoTerminadoRepository = productoTerminadoRepository;
        this.recetaRepository = recetaRepository;
        this.materiaPrimaRepository = materiaPrimaRepository;
    }

    public List<ProduccionResponse> listar() {
        return produccionRepository.findAll().stream()
                .map(ProduccionResponse::fromEntity)
                .toList();
    }

    public List<ProduccionResponse> listarPorPeriodo(LocalDate desde, LocalDate hasta) {
        return produccionRepository.findByFechaBetweenOrderByFechaDesc(desde, hasta).stream()
                .map(ProduccionResponse::fromEntity)
                .toList();
    }

    public ProduccionResponse obtener(Long id) {
        return ProduccionResponse.fromEntity(
                produccionRepository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Produccion no encontrada")));
    }

    @Transactional
    public ProduccionResponse crear(ProduccionRequest request) {
        ProductoTerminado pt = productoTerminadoRepository
                .findById(request.productoTerminadoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto terminado no encontrado"));

        Receta receta = recetaRepository.findByProductoTerminadoId(pt.getId())
                .orElseThrow(() -> new SolicitudInvalidaException("El producto no tiene una receta definida"));

        receta.getDetalles().forEach(d -> {
            MateriaPrima mp = d.getMateriaPrima();
            double cantidadNecesaria = d.getCantidad() * request.cantidadFabricada();
            if (mp.getStockActual() < cantidadNecesaria) {
                throw new StockInsuficienteException("Stock insuficiente de " + mp.getNombre());
            }
            mp.setStockActual(mp.getStockActual() - cantidadNecesaria);
            materiaPrimaRepository.save(mp);
        });

        pt.setStockActual(pt.getStockActual() + request.cantidadFabricada());
        productoTerminadoRepository.save(pt);

        Produccion produccion = new Produccion();
        produccion.setProductoTerminado(pt);
        produccion.setFecha(request.fecha());
        produccion.setCantidadFabricada(request.cantidadFabricada());

        return ProduccionResponse.fromEntity(produccionRepository.save(produccion));
    }

    public void eliminar(Long id) {
        produccionRepository.deleteById(id);
    }
}
