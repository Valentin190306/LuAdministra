package com.luadministra.compra;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    Page<Compra> findByMateriaPrimaId(Long materiaPrimaId, Pageable pageable);

    long countByMateriaPrimaId(Long materiaPrimaId);

    @Query("SELECT c FROM Compra c WHERE c.fecha = (SELECT MAX(c2.fecha) FROM Compra c2 WHERE c2.materiaPrima.id = c.materiaPrima.id)")
    List<Compra> findLatestCompraForEachMateriaPrima();

}
