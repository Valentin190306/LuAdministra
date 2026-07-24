package com.luadministra.despacho;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DespachoRepository extends JpaRepository<Despacho, Long> {

    Page<Despacho> findByColaboradoraId(Long colaboradoraId, Pageable pageable);
    Page<Despacho> findByColaboradoraIdAndEstado(Long colaboradoraId, EstadoDespacho estado, Pageable pageable);
    Page<Despacho> findByEstado(EstadoDespacho estado, Pageable pageable);

    @Query("SELECT d FROM Despacho d WHERE d.colaboradora.id = :colaboradoraId AND d.estado <> 'RENDIDO_TOTAL'")
    List<Despacho> findActivosByColaboradoraId(Long colaboradoraId);

    @Query("SELECT ld FROM LineaDespacho ld JOIN ld.despacho d WHERE ld.productoTerminado.id = :productoTerminadoId AND d.estado <> :estado")
    List<LineaDespacho> findLineasByProductoTerminadoIdAndEstadoNot(Long productoTerminadoId, EstadoDespacho estado);

    @Query("SELECT d FROM Despacho d WHERE d.estado <> 'RENDIDO_TOTAL'")
    List<Despacho> findAllActivos();
}
