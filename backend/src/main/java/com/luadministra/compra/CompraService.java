package com.luadministra.compra;

import com.luadministra.dto.PaginatedResponse;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.materiaprima.MateriaPrima;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompraService {

    private final CompraRepository compraRepository;
    private final MateriaPrimaRepository materiaPrimaRepository;

    public CompraService(CompraRepository compraRepository, MateriaPrimaRepository materiaPrimaRepository) {
        this.compraRepository = compraRepository;
        this.materiaPrimaRepository = materiaPrimaRepository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<CompraResponse> listar(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(sortDir != null && sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "fecha");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Compra> compraPage = compraRepository.findAll(pageable);
        List<CompraResponse> content = compraPage.stream().map(CompraResponse::fromEntity).toList();
        return PaginatedResponse.from(compraPage, content);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<CompraResponse> listarPorMateriaPrima(Long materiaPrimaId, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(sortDir != null && sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "fecha");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Compra> compraPage = compraRepository.findByMateriaPrimaId(materiaPrimaId, pageable);
        List<CompraResponse> content = compraPage.stream().map(CompraResponse::fromEntity).toList();
        return PaginatedResponse.from(compraPage, content);
    }

    @Transactional(readOnly = true)
    public CompraResponse obtener(Long id) {
        return CompraResponse.fromEntity(
                compraRepository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Compra no encontrada")));
    }

    @Transactional
    public CompraResponse crear(CompraRequest request) {
        MateriaPrima mp = materiaPrimaRepository.findById(request.materiaPrimaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Materia prima no encontrada"));

        Compra compra = new Compra();
        compra.setMateriaPrima(mp);
        compra.setFecha(request.fecha());
        compra.setCantidad(request.cantidad());
        compra.setPrecio(request.precio());
        compra.setLugar(request.lugar());
        compra.setUrl(request.url());
        compra.setPrecioMLReferencia(request.precioMLReferencia());

        mp.setStockActual(mp.getStockActual() + request.cantidad());
        materiaPrimaRepository.save(mp);

        return CompraResponse.fromEntity(compraRepository.save(compra));
    }

    @Transactional
    public void eliminar(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Compra no encontrada"));
        MateriaPrima mp = compra.getMateriaPrima();
        mp.setStockActual(mp.getStockActual() - compra.getCantidad());
        materiaPrimaRepository.save(mp);
        compraRepository.delete(compra);
    }
}
