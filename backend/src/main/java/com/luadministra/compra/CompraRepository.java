package com.luadministra.compra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByMateriaPrimaId(Long materiaPrimaId, Sort sort);
}
