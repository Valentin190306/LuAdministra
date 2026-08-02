package com.luadministra.venta;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    Page<Venta> findByFechaBetween(LocalDate desde, LocalDate hasta, Pageable pageable);

    @Query("SELECT COUNT(lv) FROM LineaVenta lv WHERE lv.producto.id = :productoId")
    long countLineasByProductoId(Long productoId);
}
