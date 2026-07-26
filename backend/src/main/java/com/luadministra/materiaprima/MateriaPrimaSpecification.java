package com.luadministra.materiaprima;

import org.springframework.data.jpa.domain.Specification;

public class MateriaPrimaSpecification {

    public static Specification<MateriaPrima> nombreContains(String nombre) {
        return (root, query, cb) -> {
            if (nombre == null || nombre.isBlank()) return null;
            return cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%");
        };
    }

    public static Specification<MateriaPrima> categoriaIdEquals(Long categoriaId) {
        return (root, query, cb) -> {
            if (categoriaId == null) return null;
            return cb.equal(root.get("categoria").get("id"), categoriaId);
        };
    }
}
