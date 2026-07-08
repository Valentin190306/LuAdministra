package com.luadministra.receta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecetaRepository extends JpaRepository<Receta, Long> {
    Optional<Receta> findByProductoTerminadoId(Long productoTerminadoId);
}
