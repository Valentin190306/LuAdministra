package com.luadministra.producto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luadministra.dto.PaginatedResponse;
import com.luadministra.exception.GlobalExceptionHandler;
import com.luadministra.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
@Import(GlobalExceptionHandler.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService service;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void listar_retorna200() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());
    }

    @Test
    void listarPaginado_retornaPagina() throws Exception {
        when(service.listarPaginado(anyInt(), anyInt(), any(), any(), any(), any()))
                .thenReturn(new PaginatedResponse<>(
                        List.of(new ProductoResponse(1L, "Jabon", 50.0, 10.0, 0.0, 5.0, null, null)),
                        0, 50, 1, 1));

        mockMvc.perform(get("/api/productos")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Jabon"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void obtener_retorna200() throws Exception {
        when(service.obtener(1L)).thenReturn(new ProductoResponse(1L, "Jabon", 50.0, 10.0, 0.0, 5.0, null, null));

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Jabon"));
    }

    @Test
    void obtener_cuandoNoExiste_retorna404() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("no encontrado"));

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.tipo").value("NO_ENCONTRADO"));
    }

    @Test
    void crear_conDatosInvalidos_retorna400() throws Exception {
        String body = mapper.writeValueAsString(new ProductoRequest("", null, null, null, null));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipo").value("VALIDACION"));
    }

    @Test
    void crear_conStockNegativo_retorna400() throws Exception {
        String body = mapper.writeValueAsString(new ProductoRequest("Jabon", 50.0, -5.0, null, null));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipo").value("VALIDACION"));
    }

    @Test
    void crear_conStockMinimoNegativo_retorna400() throws Exception {
        String body = mapper.writeValueAsString(new ProductoRequest("Jabon", 50.0, 10.0, -5.0, null));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipo").value("VALIDACION"));
    }

    @Test
    void crear_conPrecioCero_retorna400() throws Exception {
        String body = mapper.writeValueAsString(new ProductoRequest("Jabon", 0.0, null, null, null));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipo").value("VALIDACION"));
    }

    @Test
    void crear_retorna200() throws Exception {
        when(service.crear(any())).thenReturn(new ProductoResponse(1L, "Jabon", 50.0, 0.0, 0.0, 5.0, null, null));

        String body = mapper.writeValueAsString(new ProductoRequest("Jabon", 50.0, null, 5.0, null));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Jabon"));
    }
}
