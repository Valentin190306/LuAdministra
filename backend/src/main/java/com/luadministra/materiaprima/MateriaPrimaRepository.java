package com.luadministra.materiaprima;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MateriaPrimaRepository extends JpaRepository<MateriaPrima, Long>, JpaSpecificationExecutor<MateriaPrima> {
}
