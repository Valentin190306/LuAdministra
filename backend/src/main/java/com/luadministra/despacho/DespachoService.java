package com.luadministra.despacho;

import com.luadministra.colaboradora.Colaboradora;
import com.luadministra.colaboradora.ColaboradoraRepository;
import com.luadministra.dto.PaginatedResponse;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.productoterminado.ProductoTerminado;
import com.luadministra.productoterminado.ProductoTerminadoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DespachoService {

    private final DespachoRepository repository;
    private final ColaboradoraRepository colaboradoraRepository;
    private final ProductoTerminadoRepository productoTerminadoRepository;

    public DespachoService(DespachoRepository repository,
                           ColaboradoraRepository colaboradoraRepository,
                           ProductoTerminadoRepository productoTerminadoRepository) {
        this.repository = repository;
        this.colaboradoraRepository = colaboradoraRepository;
        this.productoTerminadoRepository = productoTerminadoRepository;
    }

    public PaginatedResponse<DespachoResponse> listar(int page, int size, String sortBy, String sortDir,
                                                       Long colaboradoraId, String estado) {
        Sort sort = Sort.by(sortDir != null && sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "fecha");
        Pageable pageable = PageRequest.of(page, size, sort);
        EstadoDespacho estadoEnum = estado != null ? EstadoDespacho.valueOf(estado) : null;
        Page<Despacho> despachoPage;
        if (colaboradoraId != null && estadoEnum != null) {
            despachoPage = repository.findByColaboradoraIdAndEstado(colaboradoraId, estadoEnum, pageable);
        } else if (colaboradoraId != null) {
            despachoPage = repository.findByColaboradoraId(colaboradoraId, pageable);
        } else if (estadoEnum != null) {
            despachoPage = repository.findByEstado(estadoEnum, pageable);
        } else {
            despachoPage = repository.findAll(pageable);
        }
        List<DespachoResponse> content = despachoPage.stream().map(DespachoResponse::fromEntity).toList();
        return PaginatedResponse.from(despachoPage, content);
    }

    public DespachoResponse obtener(Long id) {
        return DespachoResponse.fromEntity(
                repository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Despacho no encontrado")));
    }

    @Transactional
    public DespachoResponse crear(DespachoRequest request) {
        Colaboradora colaboradora = colaboradoraRepository.findById(request.colaboradoraId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Colaboradora no encontrada"));

        Despacho despacho = new Despacho();
        despacho.setColaboradora(colaboradora);
        despacho.setFecha(request.fecha());

        for (var prodReq : request.productos()) {
            ProductoTerminado pt = productoTerminadoRepository.findById(prodReq.productoTerminadoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto terminado no encontrado"));

            LineaDespacho linea = new LineaDespacho();
            linea.setDespacho(despacho);
            linea.setProductoTerminado(pt);
            linea.setCantidad(prodReq.cantidad());
            linea.setPrecioUnitario(pt.getPrecioVenta());
            despacho.getLineas().add(linea);
        }

        return DespachoResponse.fromEntity(repository.save(despacho));
    }

    @Transactional
    public void eliminar(Long id) {
        Despacho despacho = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Despacho no encontrado"));
        repository.delete(despacho);
    }

    public Double calcularStockDespachado(Long productoTerminadoId) {
        List<LineaDespacho> activas = repository.findLineasByProductoTerminadoIdAndEstadoNot(
                productoTerminadoId, EstadoDespacho.RENDIDO_TOTAL);
        return activas.stream()
                .mapToDouble(LineaDespacho::getCantidad)
                .sum();
    }
}
