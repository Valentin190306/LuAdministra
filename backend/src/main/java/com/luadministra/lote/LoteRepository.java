package com.luadministra.lote;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LoteRepository extends JpaRepository<Lote, Long> {
    Page<Lote> findByFechaBetween(LocalDate desde, LocalDate hasta, Pageable pageable);

    List<Lote> findByProductoIdOrderByFechaDesc(Long productoId);

    List<Lote> findByDiasVigenciaIsNotNull();

    long countByProductoId(Long productoId);
}
