package com.luadministra.receta;

import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.materiaprima.MateriaPrima;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import com.luadministra.productoterminado.ProductoTerminado;
import com.luadministra.productoterminado.ProductoTerminadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class RecetaService {

    private final RecetaRepository repository;
    private final ProductoTerminadoRepository productoTerminadoRepository;
    private final MateriaPrimaRepository materiaPrimaRepository;

    public RecetaService(RecetaRepository repository,
                         ProductoTerminadoRepository productoTerminadoRepository,
                         MateriaPrimaRepository materiaPrimaRepository) {
        this.repository = repository;
        this.productoTerminadoRepository = productoTerminadoRepository;
        this.materiaPrimaRepository = materiaPrimaRepository;
    }

    public RecetaResponse obtenerPorProducto(Long productoTerminadoId) {
        return RecetaResponse.fromEntity(
                repository.findByProductoTerminadoId(productoTerminadoId)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Receta no encontrada para el producto")));
    }

    @Transactional
    public RecetaResponse guardar(RecetaRequest request) {
        ProductoTerminado pt = productoTerminadoRepository.findById(request.productoTerminadoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto terminado no encontrado"));

        Receta receta = repository.findByProductoTerminadoId(pt.getId()).orElse(new Receta());
        receta.setProductoTerminado(pt);

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

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
