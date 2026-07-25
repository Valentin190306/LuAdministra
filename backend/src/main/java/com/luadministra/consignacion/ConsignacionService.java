package com.luadministra.consignacion;

import com.luadministra.consignatario.Consignatario;
import com.luadministra.consignatario.ConsignatarioRepository;
import com.luadministra.dto.PaginatedResponse;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.producto.Producto;
import com.luadministra.producto.ProductoRepository;
import com.luadministra.rendicion.RendicionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsignacionService {

    private final ConsignacionRepository repository;
    private final ConsignatarioRepository consignatarioRepository;
    private final ProductoRepository productoRepository;
    private final RendicionRepository rendicionRepository;

    public ConsignacionService(ConsignacionRepository repository,
                               ConsignatarioRepository consignatarioRepository,
                               ProductoRepository productoRepository,
                               RendicionRepository rendicionRepository) {
        this.repository = repository;
        this.consignatarioRepository = consignatarioRepository;
        this.productoRepository = productoRepository;
        this.rendicionRepository = rendicionRepository;
    }

    public PaginatedResponse<ConsignacionResponse> listar(int page, int size, String sortBy, String sortDir,
                                                           Long consignatarioId, String estado) {
        Sort sort = Sort.by(sortDir != null && sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "fecha");
        Pageable pageable = PageRequest.of(page, size, sort);
        EstadoConsignacion estadoEnum = estado != null ? EstadoConsignacion.valueOf(estado) : null;
        Page<Consignacion> consignacionPage;
        if (consignatarioId != null && estadoEnum != null) {
            consignacionPage = repository.findByConsignatarioIdAndEstado(consignatarioId, estadoEnum, pageable);
        } else if (consignatarioId != null) {
            consignacionPage = repository.findByConsignatarioId(consignatarioId, pageable);
        } else if (estadoEnum != null) {
            consignacionPage = repository.findByEstado(estadoEnum, pageable);
        } else {
            consignacionPage = repository.findAll(pageable);
        }
        List<ConsignacionResponse> content = consignacionPage.stream().map(ConsignacionResponse::fromEntity).toList();
        return PaginatedResponse.from(consignacionPage, content);
    }

    public ConsignacionResponse obtener(Long id) {
        return ConsignacionResponse.fromEntity(
                repository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Consignacion no encontrada")));
    }

    @Transactional
    public ConsignacionResponse crear(ConsignacionRequest request) {
        Consignatario consignatario = consignatarioRepository.findById(request.consignatarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Consignatario no encontrado"));

        Consignacion consignacion = new Consignacion();
        consignacion.setConsignatario(consignatario);
        consignacion.setFecha(request.fecha());

        for (var prodReq : request.productos()) {
            Producto producto = productoRepository.findById(prodReq.productoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));

            LineaConsignacion linea = new LineaConsignacion();
            linea.setConsignacion(consignacion);
            linea.setProducto(producto);
            linea.setCantidad(prodReq.cantidad());
            linea.setPrecioUnitario(producto.getPrecioVenta());
            consignacion.getLineas().add(linea);
        }

        return ConsignacionResponse.fromEntity(repository.save(consignacion));
    }

    @Transactional
    public void eliminar(Long id) {
        Consignacion consignacion = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Consignacion no encontrada"));
        repository.delete(consignacion);
    }

    public List<StockConsignadoResponse> stockConsignado(Long consignatarioId) {
        if (!consignatarioRepository.existsById(consignatarioId)) {
            throw new RecursoNoEncontradoException("Consignatario no encontrado");
        }

        List<Consignacion> activos = repository.findActivosByConsignatarioId(consignatarioId);

        Map<Long, Double> consignadoPorProducto = activos.stream()
                .flatMap(c -> c.getLineas().stream())
                .collect(Collectors.groupingBy(
                        lc -> lc.getProducto().getId(),
                        Collectors.summingDouble(LineaConsignacion::getCantidad)
                ));

        Map<Long, Double> rendidoPorProducto = activos.stream()
                .flatMap(c -> rendicionRepository.findByConsignacionId(c.getId()).stream())
                .flatMap(r -> r.getLineas().stream())
                .collect(Collectors.groupingBy(
                        lr -> lr.getLineaConsignacion().getProducto().getId(),
                        Collectors.summingDouble(lr -> lr.getCantidadVendida() + lr.getCantidadDevuelta())
                ));

        List<StockConsignadoResponse> result = new ArrayList<>();
        for (var entry : consignadoPorProducto.entrySet()) {
            Long pId = entry.getKey();
            Double consignado = entry.getValue();
            Double rendido = rendidoPorProducto.getOrDefault(pId, 0.0);
            String nombre = activos.stream()
                    .flatMap(c -> c.getLineas().stream())
                    .filter(lc -> lc.getProducto().getId().equals(pId))
                    .findFirst()
                    .map(lc -> lc.getProducto().getNombre())
                    .orElse("");
            result.add(new StockConsignadoResponse(pId, nombre, consignado, rendido, consignado - rendido));
        }

        return result;
    }
}
