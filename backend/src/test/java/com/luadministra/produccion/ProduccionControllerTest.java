package com.luadministra.produccion;

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

@WebMvcTest(ProduccionController.class)
@Import(GlobalExceptionHandler.class)
class ProduccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProduccionService service;

    @Test
    void listar_retorna200() throws Exception {
        when(service.listar(anyInt(), anyInt(), eq("fecha"), eq("desc")))
                .thenReturn(new PaginatedResponse<>(List.of(new ProduccionResponse(
                        1L, 1L, "Jabón de Lavanda",
                        LocalDate.of(2025, 3, 1), 10.0, null, null)), 0, 50, 1, 1));

        mockMvc.perform(get("/api/producciones")
                        .param("sortBy", "fecha")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productoTerminadoNombre").value("Jabón de Lavanda"));
    }

    @Test
    void obtener_retorna200() throws Exception {
        when(service.obtener(1L)).thenReturn(new ProduccionResponse(
                1L, 1L, "Jabón de Lavanda",
                LocalDate.of(2025, 3, 1), 10.0, null, null));

        mockMvc.perform(get("/api/producciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productoTerminadoNombre").value("Jabón de Lavanda"));
    }

    @Test
    void obtener_cuandoNoExiste_retorna404() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("no encontrada"));

        mockMvc.perform(get("/api/producciones/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void periodo_retorna200() throws Exception {
        when(service.listarPorPeriodo(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                0, 50))
                .thenReturn(new PaginatedResponse<>(List.of(new ProduccionResponse(
                        1L, 1L, "Jabón de Lavanda",
                        LocalDate.of(2025, 6, 15), 10.0, null, null)), 0, 50, 1, 1));

        mockMvc.perform(get("/api/producciones/periodo")
                        .param("desde", "2025-01-01")
                        .param("hasta", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productoTerminadoNombre").value("Jabón de Lavanda"));
    }
}
