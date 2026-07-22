package com.luadministra.compra;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    Page<Compra> findByMateriaPrimaId(Long materiaPrimaId, Pageable pageable);
}
