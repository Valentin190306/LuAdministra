package com.luadministra.compra;

import com.luadministra.dto.PaginatedResponse;
import com.luadministra.exception.GlobalExceptionHandler;
import com.luadministra.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompraController.class)
@Import(GlobalExceptionHandler.class)
class CompraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompraService service;

    @Test
    void listar_retorna200() throws Exception {
        when(service.listar(anyInt(), anyInt(), eq("fecha"), eq("desc")))
                .thenReturn(new PaginatedResponse<>(List.of(new CompraResponse(
                        1L, 1L, "Aceite de Coco",
                        LocalDate.of(2025, 1, 15), 500.0, 2500.0, "Juan", null)), 0, 50, 1, 1));

        mockMvc.perform(get("/api/compras")
                        .param("sortBy", "fecha")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].materiaPrimaNombre").value("Aceite de Coco"));
    }

    @Test
    void obtener_retorna200() throws Exception {
        when(service.obtener(1L)).thenReturn(new CompraResponse(
                1L, 1L, "Aceite de Coco",
                LocalDate.of(2025, 1, 15), 500.0, 2500.0, "Juan", null));

        mockMvc.perform(get("/api/compras/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materiaPrimaNombre").value("Aceite de Coco"));
    }

    @Test
    void obtener_cuandoNoExiste_retorna404() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("no encontrada"));

        mockMvc.perform(get("/api/compras/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarPorMateriaPrima_retorna200() throws Exception {
        when(service.listarPorMateriaPrima(eq(1L), anyInt(), anyInt(), eq("fecha"), eq("desc")))
                .thenReturn(new PaginatedResponse<>(List.of(new CompraResponse(
                        1L, 1L, "Aceite de Coco",
                        LocalDate.of(2025, 6, 15), 500.0, 2500.0, "Juan", null)), 0, 50, 1, 1));

        mockMvc.perform(get("/api/compras/materia-prima/1")
                        .param("sortBy", "fecha")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].materiaPrimaNombre").value("Aceite de Coco"));
    }
}
