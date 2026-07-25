package com.luadministra.receta;

import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.SolicitudInvalidaException;
import com.luadministra.materiaprima.MateriaPrima;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import com.luadministra.producto.Producto;
import com.luadministra.producto.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class RecetaService {

    private final RecetaRepository repository;
    private final ProductoRepository productoRepository;
    private final MateriaPrimaRepository materiaPrimaRepository;

    public RecetaService(RecetaRepository repository,
                         ProductoRepository productoRepository,
                         MateriaPrimaRepository materiaPrimaRepository) {
        this.repository = repository;
        this.productoRepository = productoRepository;
        this.materiaPrimaRepository = materiaPrimaRepository;
    }

    public RecetaResponse obtenerPorProducto(Long productoId) {
        return RecetaResponse.fromEntity(
                repository.findByProductoId(productoId)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Receta no encontrada para el producto")));
    }

    @Transactional
    public RecetaResponse guardar(RecetaRequest request) {
        Producto producto = productoRepository.findById(request.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));

        Receta receta = repository.findByProductoId(producto.getId()).orElse(new Receta());
        receta.setProducto(producto);
        receta.setNotas(request.notas());

        receta.getDetalles().clear();
        request.detalles().forEach(d -> {
            MateriaPrima mp = materiaPrimaRepository.findById(d.materiaPrimaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Materia prima no encontrada"));
            RecetaDetalle detalle = new RecetaDetalle();
            detalle.setReceta(receta);
            detalle.setMateriaPrima(mp);
            detalle.setCantidad(d.cantidad());
            receta.getDetalles().add(detalle);
        });

        return RecetaResponse.fromEntity(repository.save(receta));
    }

    @Transactional
    public RecetaResponse actualizar(Long id, RecetaRequest request) {
        Receta receta = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Receta no encontrada"));

        if (!receta.getProducto().getId().equals(request.productoId())) {
            throw new SolicitudInvalidaException("El producto no coincide con la receta");
        }
        receta.setNotas(request.notas());

        receta.getDetalles().clear();
        request.detalles().forEach(d -> {
            MateriaPrima mp = materiaPrimaRepository.findById(d.materiaPrimaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Materia prima no encontrada"));
            RecetaDetalle detalle = new RecetaDetalle();
            detalle.setReceta(receta);
            detalle.setMateriaPrima(mp);
            detalle.setCantidad(d.cantidad());
            receta.getDetalles().add(detalle);
        });

        return RecetaResponse.fromEntity(repository.save(receta));
    }

    public List<RecetaResponse> obtenerTodas() {
        return repository.findAll().stream()
                .map(RecetaResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Receta no encontrada");
        }
        repository.deleteById(id);
    }
}
