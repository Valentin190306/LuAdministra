package com.luadministra.receta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecetaDetalleRepository extends JpaRepository<RecetaDetalle, Long> {

    @Query("SELECT COUNT(DISTINCT rd.receta.id) FROM RecetaDetalle rd WHERE rd.materiaPrima.id = :materiaPrimaId")
    long countRecetasByMateriaPrimaId(@Param("materiaPrimaId") Long materiaPrimaId);

    @Query("SELECT COUNT(DISTINCT rd.receta.id) FROM RecetaDetalle rd WHERE rd.receta.producto.id = :productoId")
    long countRecetasByProductoId(@Param("productoId") Long productoId);
}
