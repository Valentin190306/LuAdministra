package com.luadministra.produccion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ProduccionRepository extends JpaRepository<Produccion, Long> {
    Page<Produccion> findByFechaBetween(LocalDate desde, LocalDate hasta, Pageable pageable);

    List<Produccion> findByProductoTerminadoIdOrderByFechaDesc(Long productoTerminadoId);

    List<Produccion> findByDiasVigenciaIsNotNull();
}
