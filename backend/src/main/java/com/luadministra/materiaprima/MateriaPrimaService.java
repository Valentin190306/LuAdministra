package com.luadministra.materiaprima;

import com.luadministra.categoria.CategoriaRepository;
import com.luadministra.compra.Compra;
import com.luadministra.compra.CompraRepository;
import com.luadministra.exception.RecursoNoEncontradoException;
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

    public MateriaPrimaService(MateriaPrimaRepository repository,
                               CategoriaRepository categoriaRepository,
                               CompraRepository compraRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.compraRepository = compraRepository;
    }

    public List<MateriaPrimaResponse> listar(String nombre, Long categoriaId, String sortBy, String sortDir) {
        boolean desc = sortDir != null && sortDir.equalsIgnoreCase("desc");

        Specification<MateriaPrima> spec = Specification
                .where(MateriaPrimaSpecification.nombreContains(nombre))
                .and(MateriaPrimaSpecification.categoriaIdEquals(categoriaId));

        List<MateriaPrima> materiasPrimas;

        if ("ultimaCompraFecha".equals(sortBy) || "ultimaCompraPrecio".equals(sortBy)) {
            materiasPrimas = repository.findAll(spec);
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
            materiasPrimas = materiasPrimas.stream().sorted(comparator).toList();
        } else {
            String effectiveSortBy = sortBy != null ? sortBy : "nombre";
            Sort sort = Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, effectiveSortBy);
            materiasPrimas = repository.findAll(spec, sort);
        }

        return materiasPrimas.stream().map(MateriaPrimaResponse::fromEntity).toList();
    }

    public MateriaPrimaResponse obtener(Long id) {
        return MateriaPrimaResponse.fromEntity(
                repository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Materia prima no encontrada")));
    }

    @Transactional
    public MateriaPrimaResponse crear(MateriaPrimaRequest request) {
        MateriaPrima mp = new MateriaPrima();
        mp.setNombre(request.nombre());
        mp.setUnidadMedida(request.unidadMedida());
        mp.setStockMinimo(request.stockMinimo());
        mp.setStockActual(0.0);
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
        repository.deleteById(id);
    }
}
