package com.luadministra.ml;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultaPrecioMLRepository extends JpaRepository<ConsultaPrecioML, Long> {

    List<ConsultaPrecioML> findByCompraIdOrderByFechaHoraDesc(Long compraId);

    java.util.Optional<ConsultaPrecioML> findFirstByCompraIdOrderByFechaHoraDesc(Long compraId);
}
