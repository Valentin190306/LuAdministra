package com.luadministra.productoterminado;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoTerminadoController.class)
@Import(GlobalExceptionHandler.class)
class ProductoTerminadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoTerminadoService service;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void listar_retorna200() throws Exception {
        when(service.listar(eq("Jabón"), eq(1L), eq("nombre"), eq("asc")))
                .thenReturn(List.of(new ProductoTerminadoResponse(
                        1L, "Jabón de Lavanda", 2000.0, 0.0, 0.0, null, null, null)));

        mockMvc.perform(get("/api/productos-terminados")
                        .param("nombre", "Jabón")
                        .param("categoriaId", "1")
                        .param("sortBy", "nombre")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Jabón de Lavanda"));
    }

    @Test
    void obtener_retorna200() throws Exception {
        when(service.obtener(1L)).thenReturn(new ProductoTerminadoResponse(
                1L, "Jabón de Lavanda", 2000.0, 0.0, 0.0, null, null, null));

        mockMvc.perform(get("/api/productos-terminados/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Jabón de Lavanda"));
    }

    @Test
    void obtener_cuandoNoExiste_retorna404() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("no encontrado"));

        mockMvc.perform(get("/api/productos-terminados/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.tipo").value("NO_ENCONTRADO"));
    }

    @Test
    void crear_conDatosInvalidos_retorna400() throws Exception {
        String body = mapper.writeValueAsString(new ProductoTerminadoRequest("", 0.0, null, null, null));

        mockMvc.perform(post("/api/productos-terminados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipo").value("VALIDACION"));
    }

    @Test
    void crear_retorna200() throws Exception {
        when(service.crear(any())).thenReturn(new ProductoTerminadoResponse(
                1L, "Jabón de Lavanda", 2000.0, 0.0, 0.0, null, null, null));

        String body = mapper.writeValueAsString(new ProductoTerminadoRequest("Jabón de Lavanda", 2000.0, null, null, null));

        mockMvc.perform(post("/api/productos-terminados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Jabón de Lavanda"));
    }
}
