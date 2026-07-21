package com.luadministra.receta;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecetaController.class)
@Import(GlobalExceptionHandler.class)
class RecetaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecetaService service;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void obtenerPorProducto_retorna200() throws Exception {
        when(service.obtenerPorProducto(1L)).thenReturn(new RecetaResponse(
                1L, 1L, "Producto 1", List.of(), null));

        mockMvc.perform(get("/api/recetas/producto/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productoTerminadoNombre").value("Producto 1"));
    }

    @Test
    void obtenerPorProducto_cuandoNoExiste_retorna404() throws Exception {
        when(service.obtenerPorProducto(99L)).thenThrow(new RecursoNoEncontradoException("no encontrada"));

        mockMvc.perform(get("/api/recetas/producto/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.tipo").value("NO_ENCONTRADO"));
    }

    @Test
    void crear_conDatosInvalidos_retorna400() throws Exception {
        String body = mapper.writeValueAsString(new RecetaRequest(null, List.of(), null));

        mockMvc.perform(post("/api/recetas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipo").value("VALIDACION"));
    }

    @Test
    void crear_retorna200() throws Exception {
        when(service.guardar(any())).thenReturn(new RecetaResponse(
                1L, 1L, "Producto 1", List.of(), null));

        String body = mapper.writeValueAsString(new RecetaRequest(1L, List.of(new RecetaDetalleRequest(10L, 50.0)), null));

        mockMvc.perform(post("/api/recetas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void actualizar_retorna200() throws Exception {
        when(service.actualizar(any(), any())).thenReturn(new RecetaResponse(
                1L, 1L, "Producto 1", List.of(), null));

        String body = mapper.writeValueAsString(new RecetaRequest(1L, List.of(), null));

        mockMvc.perform(put("/api/recetas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productoTerminadoNombre").value("Producto 1"));
    }

    @Test
    void eliminar_retorna204() throws Exception {
        mockMvc.perform(delete("/api/recetas/1"))
                .andExpect(status().isNoContent());
    }
}
