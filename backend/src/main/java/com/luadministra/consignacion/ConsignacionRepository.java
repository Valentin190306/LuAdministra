package com.luadministra.consignacion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConsignacionRepository extends JpaRepository<Consignacion, Long> {

    Page<Consignacion> findByConsignatarioId(Long consignatarioId, Pageable pageable);
    Page<Consignacion> findByConsignatarioIdAndEstado(Long consignatarioId, EstadoConsignacion estado, Pageable pageable);
    Page<Consignacion> findByEstado(EstadoConsignacion estado, Pageable pageable);

    @Query("SELECT c FROM Consignacion c WHERE c.consignatario.id = :consignatarioId AND c.estado <> 'RENDIDO_TOTAL'")
    List<Consignacion> findActivosByConsignatarioId(Long consignatarioId);

    @Query("SELECT lc FROM LineaConsignacion lc JOIN lc.consignacion c WHERE lc.producto.id = :productoId AND c.estado <> :estado")
    List<LineaConsignacion> findLineasByProductoIdAndEstadoNot(Long productoId, EstadoConsignacion estado);

    @Query("SELECT c FROM Consignacion c WHERE c.estado <> 'RENDIDO_TOTAL'")
    List<Consignacion> findAllActivos();

    @Query("SELECT COUNT(lc) FROM LineaConsignacion lc WHERE lc.producto.id = :productoId")
    long countLineasByProductoId(Long productoId);

    long countByConsignatarioId(Long consignatarioId);
}
