package com.luadministra.venta;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.luadministra.exception.GlobalExceptionHandler;
import com.luadministra.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VentaController.class)
@Import(GlobalExceptionHandler.class)
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VentaService service;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void listar_retorna200() throws Exception {
        when(service.listar(eq("fecha"), eq("desc")))
                .thenReturn(List.of(new VentaResponse(
                        1L, 1L, "Jabón de Lavanda",
                        LocalDate.of(2025, 6, 1), 5.0, 2000.0)));

        mockMvc.perform(get("/api/ventas")
                        .param("sortBy", "fecha")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productoTerminadoNombre").value("Jabón de Lavanda"));
    }

    @Test
    void obtener_retorna200() throws Exception {
        when(service.obtener(1L)).thenReturn(new VentaResponse(
                1L, 1L, "Jabón de Lavanda",
                LocalDate.of(2025, 6, 1), 5.0, 2000.0));

        mockMvc.perform(get("/api/ventas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productoTerminadoNombre").value("Jabón de Lavanda"));
    }

    @Test
    void obtener_cuandoNoExiste_retorna404() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("no encontrada"));

        mockMvc.perform(get("/api/ventas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_conDatosInvalidos_retorna400() throws Exception {
        String body = mapper.writeValueAsString(new VentaRequest(null, LocalDate.now(), 0.0));

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipo").value("VALIDACION"));
    }

    @Test
    void crear_retorna200() throws Exception {
        when(service.crear(any())).thenReturn(new VentaResponse(
                1L, 1L, "Jabón de Lavanda",
                LocalDate.of(2025, 6, 1), 5.0, 2000.0));

        String body = mapper.writeValueAsString(new VentaRequest(1L, LocalDate.of(2025, 6, 1), 5.0));

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productoTerminadoNombre").value("Jabón de Lavanda"));
    }

    @Test
    void periodo_retorna200() throws Exception {
        when(service.listarPorPeriodo(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)))
                .thenReturn(List.of(new VentaResponse(
                        1L, 1L, "Jabón de Lavanda",
                        LocalDate.of(2025, 6, 15), 5.0, 2000.0)));

        mockMvc.perform(get("/api/ventas/periodo")
                        .param("desde", "2025-01-01")
                        .param("hasta", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productoTerminadoNombre").value("Jabón de Lavanda"));
    }
}
