package com.luadministra.venta;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    Page<Venta> findByFechaBetween(LocalDate desde, LocalDate hasta, Pageable pageable);
}
