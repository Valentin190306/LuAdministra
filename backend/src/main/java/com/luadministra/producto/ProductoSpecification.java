package com.luadministra.producto;

import org.springframework.data.jpa.domain.Specification;

public class ProductoSpecification {

    public static Specification<Producto> nombreContains(String nombre) {
        return (root, query, cb) -> {
            if (nombre == null || nombre.isBlank()) return null;
            return cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%");
        };
    }

    public static Specification<Producto> categoriaIdEquals(Long categoriaId) {
        return (root, query, cb) -> {
            if (categoriaId == null) return null;
            return cb.equal(root.get("categoria").get("id"), categoriaId);
        };
    }
}
