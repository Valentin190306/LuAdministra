package com.luadministra.materiaprima;

import com.luadministra.categoria.CategoriaRepository;
import com.luadministra.compra.Compra;
import com.luadministra.compra.CompraRepository;
import com.luadministra.dto.PaginatedResponse;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.SolicitudInvalidaException;
import com.luadministra.receta.RecetaDetalleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MateriaPrimaService {

    private final MateriaPrimaRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final CompraRepository compraRepository;
    private final RecetaDetalleRepository recetaDetalleRepository;

    public MateriaPrimaService(MateriaPrimaRepository repository,
                               CategoriaRepository categoriaRepository,
                               CompraRepository compraRepository,
                               RecetaDetalleRepository recetaDetalleRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.compraRepository = compraRepository;
        this.recetaDetalleRepository = recetaDetalleRepository;
    }

    public List<MateriaPrimaResponse> listar(String nombre, Long categoriaId, String sortBy, String sortDir) {
        boolean desc = sortDir != null && sortDir.equalsIgnoreCase("desc");

        Specification<MateriaPrima> spec = Specification
                .where(MateriaPrimaSpecification.nombreContains(nombre))
                .and(MateriaPrimaSpecification.categoriaIdEquals(categoriaId));

        if ("ultimaCompraFecha".equals(sortBy) || "ultimaCompraPrecio".equals(sortBy)) {
            return ordenarPorUltimaCompra(spec, sortBy, desc).stream()
                    .map(MateriaPrimaResponse::fromEntity)
                    .toList();
        }

        String effectiveSortBy = sortBy != null ? sortBy : "nombre";
        Sort sort = Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, effectiveSortBy);
        return repository.findAll(spec, sort).stream().map(MateriaPrimaResponse::fromEntity).toList();
    }

    public PaginatedResponse<MateriaPrimaResponse> listarPaginado(int page, int size, String nombre, Long categoriaId, String sortBy, String sortDir) {
        boolean desc = sortDir != null && sortDir.equalsIgnoreCase("desc");

        Specification<MateriaPrima> spec = Specification
                .where(MateriaPrimaSpecification.nombreContains(nombre))
                .and(MateriaPrimaSpecification.categoriaIdEquals(categoriaId));

        if ("ultimaCompraFecha".equals(sortBy) || "ultimaCompraPrecio".equals(sortBy)) {
            List<MateriaPrima> ordenadas = ordenarPorUltimaCompra(spec, sortBy, desc);
            int from = Math.min(page * size, ordenadas.size());
            int to = Math.min(from + size, ordenadas.size());
            List<MateriaPrimaResponse> content = ordenadas.subList(from, to).stream()
                    .map(MateriaPrimaResponse::fromEntity)
                    .toList();
            int totalPages = (int) Math.ceil((double) ordenadas.size() / size);
            return new PaginatedResponse<>(content, page, size, ordenadas.size(), totalPages);
        }

        String effectiveSortBy = sortBy != null ? sortBy : "nombre";
        Sort sort = Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, effectiveSortBy);
        Page<MateriaPrima> result = repository.findAll(spec, PageRequest.of(page, size, sort));
        List<MateriaPrimaResponse> content = result.getContent().stream().map(MateriaPrimaResponse::fromEntity).toList();
        return PaginatedResponse.from(result, content);
    }

    private List<MateriaPrima> ordenarPorUltimaCompra(Specification<MateriaPrima> spec, String sortBy, boolean desc) {
        List<MateriaPrima> materiasPrimas = repository.findAll(spec);
        List<Compra> latestCompras = compraRepository.findLatestCompraForEachMateriaPrima();
        Map<Long, Compra> latestByMpId = latestCompras.stream()
                .collect(Collectors.toMap(c -> c.getMateriaPrima().getId(), c -> c));
        Comparator<MateriaPrima> comparator;
        if ("ultimaCompraFecha".equals(sortBy)) {
            comparator = Comparator.comparing(
                    (MateriaPrima mp) -> {
                        Compra c = latestByMpId.get(mp.getId());
                        return c != null ? c.getFecha() : LocalDate.MIN;
                    },
                    desc ? Comparator.reverseOrder() : Comparator.naturalOrder()
            );
        } else {
            comparator = Comparator.comparing(
                    (MateriaPrima mp) -> {
                        Compra c = latestByMpId.get(mp.getId());
                        return c != null ? c.getPrecio() : Double.MIN_VALUE;
                    },
                    desc ? Comparator.reverseOrder() : Comparator.naturalOrder()
            );
        }
        return materiasPrimas.stream().sorted(comparator).toList();
    }

    public MateriaPrimaResponse obtener(Long id) {
        return MateriaPrimaResponse.fromEntity(
                repository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Materia prima no encontrada")));
    }

    @Transactional
    public MateriaPrimaResponse crear(MateriaPrimaRequest request) {
        MateriaPrima mp = new MateriaPrima();
        Double stockActual = request.stockActual();
        mp.setNombre(request.nombre());
        mp.setUnidadMedida(request.unidadMedida());
        mp.setStockMinimo(request.stockMinimo());
        mp.setStockActual(stockActual != null ? stockActual.doubleValue() : 0.0);
        if (request.categoriaId() != null) {
            mp.setCategoria(categoriaRepository.findById(request.categoriaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada")));
        }
        return MateriaPrimaResponse.fromEntity(repository.save(mp));
    }

    @Transactional
    public MateriaPrimaResponse actualizar(Long id, MateriaPrimaRequest request) {
        MateriaPrima existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Materia prima no encontrada"));
        existente.setNombre(request.nombre());
        existente.setUnidadMedida(request.unidadMedida());
        existente.setStockMinimo(request.stockMinimo());
        if (request.stockActual() != null) {
            existente.setStockActual(request.stockActual());
        }
        if (request.categoriaId() != null) {
            existente.setCategoria(categoriaRepository.findById(request.categoriaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada")));
        } else {
            existente.setCategoria(null);
        }
        return MateriaPrimaResponse.fromEntity(repository.save(existente));
    }

    @Transactional
    public void eliminar(Long id) {
        long recetas = recetaDetalleRepository.countRecetasByMateriaPrimaId(id);
        long compras = compraRepository.countByMateriaPrimaId(id);
        if (recetas > 0 || compras > 0) {
            throw new SolicitudInvalidaException("No se puede eliminar: está en uso en " + recetas
                    + " receta(s) y " + compras + " compra(s)");
        }
        repository.deleteById(id);
    }
}
