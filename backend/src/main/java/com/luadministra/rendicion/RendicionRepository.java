package com.luadministra.rendicion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RendicionRepository extends JpaRepository<Rendicion, Long> {

    List<Rendicion> findByConsignacionId(Long consignacionId);
    Page<Rendicion> findByConsignacionId(Long consignacionId, Pageable pageable);
    List<Rendicion> findByConsignacionIdIn(List<Long> ids);

    long countByConsignacionId(Long consignacionId);
}
