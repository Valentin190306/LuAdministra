package com.luadministra.compra;

import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.materiaprima.MateriaPrima;
import com.luadministra.materiaprima.MateriaPrimaRepository;
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

    public List<CompraResponse> listar() {
        return compraRepository.findAll().stream()
                .map(CompraResponse::fromEntity)
                .toList();
    }

    public List<CompraResponse> listarPorMateriaPrima(Long materiaPrimaId) {
        return compraRepository.findByMateriaPrimaIdOrderByFechaDesc(materiaPrimaId).stream()
                .map(CompraResponse::fromEntity)
                .toList();
    }

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
