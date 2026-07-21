package com.luadministra.categoriaproductoterminado;

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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaProductoTerminadoController.class)
@Import(GlobalExceptionHandler.class)
class CategoriaProductoTerminadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaProductoTerminadoService service;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void listar_retorna200() throws Exception {
        when(service.listar()).thenReturn(List.of(
                new CategoriaProductoTerminadoResponse(1L, "Shampoos", null, null)));

        mockMvc.perform(get("/api/categorias-productos-terminados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Shampoos"));
    }

    @Test
    void obtener_retorna200() throws Exception {
        when(service.obtener(1L)).thenReturn(
                new CategoriaProductoTerminadoResponse(1L, "Shampoos", null, null));

        mockMvc.perform(get("/api/categorias-productos-terminados/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Shampoos"));
    }

    @Test
    void obtener_cuandoNoExiste_retorna404() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("no encontrada"));

        mockMvc.perform(get("/api/categorias-productos-terminados/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.tipo").value("NO_ENCONTRADO"));
    }

    @Test
    void crear_conDatosInvalidos_retorna400() throws Exception {
        String body = mapper.writeValueAsString(new CategoriaProductoTerminadoRequest("", null));

        mockMvc.perform(post("/api/categorias-productos-terminados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipo").value("VALIDACION"));
    }

    @Test
    void crear_retorna201() throws Exception {
        when(service.crear(new CategoriaProductoTerminadoRequest("Shampoos", null)))
                .thenReturn(new CategoriaProductoTerminadoResponse(1L, "Shampoos", null, null));

        String body = mapper.writeValueAsString(new CategoriaProductoTerminadoRequest("Shampoos", null));

        mockMvc.perform(post("/api/categorias-productos-terminados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}
