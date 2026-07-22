package com.luadministra.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PaginatedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PaginatedResponse<T> from(Page<?> page, List<T> content) {
        return new PaginatedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
