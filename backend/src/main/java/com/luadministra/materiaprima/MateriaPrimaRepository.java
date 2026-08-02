package com.luadministra.materiaprima;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MateriaPrimaRepository extends JpaRepository<MateriaPrima, Long>, JpaSpecificationExecutor<MateriaPrima> {
    List<MateriaPrima> findByCategoriaId(Long categoriaId);
}
