package com.luadministra.produccion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ProduccionRepository extends JpaRepository<Produccion, Long> {
    List<Produccion> findByFechaBetweenOrderByFechaDesc(LocalDate desde, LocalDate hasta);
}
